package test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import org.apache.log4j.Logger;
import test.database.ProxyRepository;
import test.database.ProxyRepositoryImpl;
import test.entity.Proxy;
import test.event.ExecuteRequestEvent;
import test.event.ProxyParseEvent;

import java.util.List;


public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(Server.class.getName());
//        vertx.setTimer(4000, aLong -> {
//            test(vertx);
//        });
    }

    private static void check(Vertx vertx) {
        vertx.createHttpClient().post(8081, "localhost", "/", httpClientResponse -> {
            httpClientResponse.bodyHandler(buffer -> {

            });
        }).end("--check=start");
    }

    private static void test(Vertx vertx) {
        int nOfRequests = 10;
        final int[] successCounter = {0};
        for (int i = 0; i < nOfRequests; i++) {
            int requestId = i;
            vertx.createHttpClient().post(8081, "localhost", "/", httpClientResponse -> {
                httpClientResponse.bodyHandler(buffer -> {
                    successCounter[0]++;
                    logger.info("request #"+ requestId + " is done");
                    if( successCounter[0]==nOfRequests) {
                        logger.info("ALL ARE DONE");
                    }
                });
            }).end("--request=google.com");
        }
    }


}
