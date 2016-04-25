package test.validation;

import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import test.event.ProxyResponseEvent;

import java.io.IOException;

/**
 * Created by user on 25.04.2016.
 */
public class ValidationYandexHeaders implements ValidationStrategy {

    private LoggingAdapter logger;

    public ValidationYandexHeaders(ActorSystem system) {
        logger = Logging.getLogger(system, this);
    }

    @Override
    public boolean validate(ProxyResponseEvent responseEvent) {
        if(!responseEvent.getUrl().contains("yandex.ru")){
            return true;
        }
        CloseableHttpResponse response = responseEvent.getResponse();
        Header[] headerToCheck = response.getHeaders("x-frame-options");
        if(headerToCheck == null || headerToCheck.length == 0) {
            logger.debug("no header");
            return false;
        }
        logger.debug("has header");
        return true;
    }
}
