package test.validation;

import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import test.event.ProxyResponseEvent;

/**
 * Created by user on 25.04.2016.
 */
public class ValidationGoogleHeaders implements ValidationStrategy {

    private LoggingAdapter logger;

    public ValidationGoogleHeaders(ActorSystem system) {
        logger = Logging.getLogger(system,this);
    }

    @Override
    public boolean validate(ProxyResponseEvent responseEvent) {
        if(responseEvent.getProxy().getCountry().equals("China")) {
            return true;
        }
        if(!responseEvent.getUrl().contains("google.com")){
            return true;
        }
        CloseableHttpResponse response = responseEvent.getResponse();
        Header[] headerToCheck = response.getHeaders("x-frame-options");
        if (headerToCheck == null || headerToCheck.length == 0) {
            logger.debug("no header");
            return false;
        }
        logger.debug("has header");
        return true;
    }
}
