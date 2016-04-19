package test.util;

import org.slf4j.LoggerFactory;

/**
 * Created by 10 on 19.04.2016.
 */
public class Logger {

    private static org.slf4j.Logger logger;

    static  {
        logger = LoggerFactory.getLogger("app");
    }

    public static void t(String msg) {
        logger.trace(msg);
    }

    public static void d(String msg) {
        logger.debug(msg);
    }

    public static void i(String msg) {
        logger.info(msg);
    }

    public static void e(String msg) {
        logger.error(msg);
    }
}
