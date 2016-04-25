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
            String body = null;
            String[] lines = new String[0];
            try {
                body = EntityUtils.toString(responseEvent.getResponse().getEntity());
                lines = body.split("\n");
            } catch (IOException e) {
                logger.debug("error");
                return false;
//                e.printStackTrace();
            }
            Header[] allHeaders = response.getAllHeaders();
            String[] a  = lines;
            String body1 = body;
            logger.debug("no header");
            return false;
        }
        logger.debug("has header");
        return true;
    }
}
