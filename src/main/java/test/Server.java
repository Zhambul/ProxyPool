package test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Inbox;
import akka.actor.Props;
import akka.japi.Creator;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import scala.concurrent.duration.Duration;
import test.database.ProxyRepository;
import test.database.ProxyRepositoryImpl;
import test.event.ExecuteRequestEvent;
import test.event.ProxyParseEvent;
import test.event.ProxyResponseEvent;
import test.event.StartCheckEvent;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by user on 22.04.2016.
 */
public class Server extends AbstractVerticle {
    private static final int MIN_TIMEOUT = 1000;
    private final int DEFAULT_TIMEOUT = 3000;
    private ProxyRepository proxyRepository = new ProxyRepositoryImpl();
    private ActorRef proxyManager;
    private final Logger logger = Logger.getLogger(Main.class.getName());
    private Inbox inbox;

    @Override
    public void start() throws Exception {
        initActorSystem();
        Router router = Router.router(vertx);

        router.post().handler(this::handlePutRequest);

        vertx.createHttpServer().requestHandler(router::accept).listen(8081);
        logger.info("ready");
    }

    private void handlePutRequest(RoutingContext routingContext) {
        OptionParser parser = initCLIParser();
        routingContext.request().bodyHandler(buffer -> {
            String[] args = buffer.toString().split(" ");
            OptionSet optionSet = null;
            try {
                optionSet = parser.parse(args);
            }
            catch (Exception e) {
                routingContext.response().setStatusCode(400).end("unrecognized option");
                return;
            }

            int timeOut = DEFAULT_TIMEOUT;
            if (optionSet.has("timeOut")) {
                if((int) optionSet.valueOf("timeOut") < MIN_TIMEOUT) {
                    routingContext.response().end("too small timeOut");
                    return;
                }
                timeOut = (int) optionSet.valueOf("timeOut");
            }
            logger.debug("timeOut is " + timeOut);
            if (optionSet.has("request")) {
                handleRequest(routingContext, optionSet, timeOut);
            }
            if (optionSet.has("check")) {
                String url = null;
                if(optionSet.has("url")) {
                    url = (String) optionSet.valueOf("url");
                }
                else if(optionSet.has("a")) {
                    url = "www.azenv.net";
                }
                handleCheck(routingContext, optionSet,timeOut,url);
            }
            if (optionSet.has("parse")) {
                handleParse(routingContext, optionSet);
            }
        });
    }

    private void handleParse(RoutingContext routingContext, OptionSet optionSet) {
        String filePath = (String) optionSet.valueOf("parse");
        logger.info("parsing " + filePath );
        ProxyParseEvent proxyParseEvent = new ProxyParseEvent(filePath);
        inbox.send(proxyManager,proxyParseEvent);
        routingContext.response().end();
    }

    private void handleCheck(RoutingContext routingContext, OptionSet optionSet, int timeOut, String url) {
        Object flag = optionSet.valueOf("check");
        if(flag.equals("start")) {
            logger.info("start checking");
            StartCheckEvent startCheckEvent = new StartCheckEvent(timeOut,url);
            inbox.send(proxyManager, startCheckEvent);
            routingContext.response().end();
        }
        else if(flag.equals("stop")){
            logger.info("stop checking");
            inbox.send(proxyManager, "stopChecking");
            routingContext.response().end();
        }
        else {
            routingContext.response().setStatusCode(400).end("unrecognised option");
        }
    }

    private void handleRequest(RoutingContext routingContext, OptionSet optionSet, int timeOut) {
        String url = (String) optionSet.valueOf("request");
        int requestId = (int) optionSet.valueOf("id");

        logger.info("request to " + url);
        ExecuteRequestEvent executeRequestEvent = new ExecuteRequestEvent(url,timeOut,requestId);
        inbox.send(proxyManager,executeRequestEvent);
        vertx.executeBlocking(objectFuture -> {
            try {
                objectFuture.complete(inbox.receive(Duration.apply(200, TimeUnit.SECONDS)));
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }, objectAsyncResult -> {
            ProxyResponseEvent proxyResponseEvent = (ProxyResponseEvent) objectAsyncResult.result();
            String body=null;
            try {
                body = EntityUtils.toString(proxyResponseEvent.getResponse().getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    proxyResponseEvent.getResponse().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            routingContext.response().putHeader("X-Request-Id", String.valueOf(proxyResponseEvent.getRequestId())).end(body);
            logger.info("success request with id " + proxyResponseEvent.getRequestId());
        });
    }

    private void initActorSystem() {
        ActorSystem system = ActorSystem.create("system");
        inbox = Inbox.create(system);

        proxyManager = system.actorOf(
                Props.create(ProxyManager.class, (Creator<ProxyManager>) () ->
                        new ProxyManager(proxyRepository)),"proxyManager");
    }

    private OptionParser initCLIParser() {
        return new OptionParser() {
            {
                accepts("request").withRequiredArg();
                accepts("check").withRequiredArg();
                accepts("a").availableIf("check");
                accepts("url").availableIf("check").availableUnless("a").withRequiredArg();
                accepts("id").requiredIf("request").withRequiredArg().ofType(Integer.class);
                accepts("timeOut").availableIf("request").availableIf("check").withRequiredArg().ofType(Integer.class);
                accepts("parse").withRequiredArg();
            }
        };
    }
}
