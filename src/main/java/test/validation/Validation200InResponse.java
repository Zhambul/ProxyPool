package test.validation;

import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import test.event.ProxyResponseEvent;


/**
 * Created by user on 25.04.2016.
 */
public class Validation200InResponse implements ValidationStrategy {

    private LoggingAdapter logger;

    public Validation200InResponse(ActorSystem system) {
        logger = Logging.getLogger(system,this);
    }

    @Override
    public boolean validate(ProxyResponseEvent responseEvent) {
        if(responseEvent.getResponse().getStatusLine().getStatusCode() == 200) {
            logger.debug("200");
            return true;
        }
        else {
            logger.debug("not 200");
            return false;
        }
    }
}
