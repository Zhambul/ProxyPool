package test.validation;

import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import test.event.ProxyResponseEvent;

/**
 * Created by user on 25.04.2016.
 */
public class ValidationNullResponse implements ValidationStrategy {

    private LoggingAdapter logger;

    public ValidationNullResponse(ActorSystem system) {
        logger = Logging.getLogger(system,this);
    }

    @Override
    public boolean validate(ProxyResponseEvent responseEvent) {
        if (responseEvent.getResponse() != null) {
            logger.debug("response in not null");
            return true;
        }
        else {
            logger.debug("response in null");
            return false;
        }

    }
}
