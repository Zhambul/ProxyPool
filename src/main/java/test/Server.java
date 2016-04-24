package test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Inbox;
import akka.actor.Props;
import akka.dispatch.Mailbox;
import akka.dispatch.Mailboxes;
import akka.japi.Creator;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by user on 22.04.2016.
 */
public class Server extends AbstractVerticle {

    private ProxyRepository proxyRepository = new ProxyRepositoryImpl();
    private ActorRef proxyManagerRef;
    private final Logger logger = Logger.getLogger(Main.class.getName());
    private Inbox inbox;

    @Override
    public void start() throws Exception {
        initActorSystem();
        Router router = Router.router(vertx);

        router.post().handler(this::handlePut);

        vertx.createHttpServer().requestHandler(router::accept).listen(8081);
    }
    private void handlePut(RoutingContext routingContext) {
        OptionParser parser = initCLIParser();
        routingContext.request().bodyHandler(buffer -> {
            String command = buffer.toString();
            OptionSet optionSet = parser.parse(command);

            if (optionSet.has("request")) {
                String url = (String) optionSet.valueOf("request");
                logger.info("request to " + url);
                ExecuteRequestEvent executeRequestEvent = new ExecuteRequestEvent(url);
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
                });
            }
            else if (optionSet.has("check")) {
                logger.info("checking");
                inbox.send(proxyManagerRef,"startMonitoring");
                routingContext.response().end();
            }
            else if (optionSet.has("parse")) {
                String filePath = (String) optionSet.valueOf("parse");
                logger.info("parsing " + filePath );
                ProxyParseEvent proxyParseEvent = new ProxyParseEvent(filePath);
                inbox.send(proxyManagerRef,proxyParseEvent);
                routingContext.response().end();
            }
        });
    }

    private void initActorSystem() {
        ActorSystem system = ActorSystem.create("system");
        inbox = Inbox.create(system);

        proxyManagerRef = system.actorOf(
                Props.create(ProxyManager.class,(Creator<ProxyManager>) () ->
                        new ProxyManager(proxyRepository)));
    }

    private OptionParser initCLIParser() {
        return new OptionParser() {
            {
                accepts("request").withRequiredArg();
                accepts("check");
                accepts("parse").withRequiredArg();
            }
        };
    }
}
