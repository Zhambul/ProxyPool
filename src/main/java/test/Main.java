package test;

import io.vertx.core.Vertx;
import org.apache.log4j.Logger;


public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(Server.class.getName());
        vertx.setTimer(4000, aLong -> {
            check(vertx);
        });
    }

    private static void check(Vertx vertx) {
        vertx.createHttpClient().post(8081, "localhost", "/", httpClientResponse -> {
            httpClientResponse.bodyHandler(buffer -> {
                System.out.println(buffer.toString());
            });
        }).end("-check start -a");
    }

    private static void test(Vertx vertx) {
        logger.info("testing");
        int nOfRequests = 10;
        final int[] successCounter = {0};
        for (int i = 0; i < nOfRequests; i++) {
            int requestId = i;
            vertx.createHttpClient().post(8081, "localhost", "/", httpClientResponse -> {
                httpClientResponse.bodyHandler(buffer -> {
                    successCounter[0]++;
                    if( successCounter[0]==nOfRequests) {
                        logger.info("ALL ARE DONE");
                    }
                });
            }).end("-request stackoverflow.com -id " + requestId);
        }
    }


}
