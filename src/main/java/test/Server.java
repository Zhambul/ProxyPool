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
import org.apache.log4j.Logger;
import scala.concurrent.duration.Duration;
import test.database.ProxyRepository;
import test.database.ProxyRepositoryImpl;
import test.event.ExecuteRequestEvent;
import test.event.ProxyParseEvent;
import test.event.StartCheckEvent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by user on 22.04.2016.
 */
class Server extends AbstractVerticle {
    private ProxyRepository proxyRepository = new ProxyRepositoryImpl();
    private ActorRef proxyManagerRef;
    private final Logger logger = Logger.getLogger(Main.class.getName());
    private Inbox inbox;
    private final int DEFAULT_TIME_OUT = 3000;

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

            int timeOut = DEFAULT_TIME_OUT;
            if (optionSet.has("timeOut")) {
                if((int) optionSet.valueOf("timeOut") < 1000) {
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
                handleCheck(routingContext, optionSet,timeOut);
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
        inbox.send(proxyManagerRef,proxyParseEvent);
        routingContext.response().end();
    }

    private void handleCheck(RoutingContext routingContext, OptionSet optionSet, int timeOut) {
        Object flag = optionSet.valueOf("check");
        if(flag.equals("start")) {
            logger.info("start checking");
            StartCheckEvent startCheckEvent = new StartCheckEvent(timeOut);
            inbox.send(proxyManagerRef, startCheckEvent);
            routingContext.response().end();
        }
        else if(flag.equals("stop")){
            logger.info("stop checking");
            inbox.send(proxyManagerRef, "stopChecking");
            routingContext.response().end();
        }
        else {
            routingContext.response().setStatusCode(400).end("unrecognised option");
        }
    }

    private void handleRequest(RoutingContext routingContext, OptionSet optionSet, int timeOut) {
        String url = (String) optionSet.valueOf("request");
        logger.info("request to " + url);
        ExecuteRequestEvent executeRequestEvent = new ExecuteRequestEvent(url,timeOut);
        inbox.send(proxyManagerRef,executeRequestEvent);
        vertx.executeBlocking(objectFuture -> {
            try {
                Object receive = inbox.receive(Duration.apply(200, TimeUnit.SECONDS));
                objectFuture.complete(receive);
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }, objectAsyncResult -> {
            Object result = objectAsyncResult.result();
            routingContext.response().end(result.toString());
            logger.info("success");
        });
    }

    private void initActorSystem() {
        ActorSystem system = ActorSystem.create("system");
        inbox = Inbox.create(system);

        proxyManagerRef = system.actorOf(
                Props.create(ProxyManager.class, (Creator<ProxyManager>) () ->
                        new ProxyManager(proxyRepository)));
    }

    private OptionParser initCLIParser() {
        return new OptionParser() {
            {
                accepts("request").withRequiredArg();
                accepts("check").withRequiredArg();
                accepts("timeOut").availableIf("request").availableIf("check").withRequiredArg()
                        .ofType(Integer.class);
                accepts("parse").withRequiredArg();
            }
        };
    }
}
