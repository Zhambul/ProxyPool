package test.validation;

import org.apache.log4j.Logger;
import test.event.ProxyResponseEvent;


/**
 * Created by user on 25.04.2016.
 */
public class Validation200InResponse implements ValidationStrategy {

    @Override
    public boolean validate(ProxyResponseEvent responseEvent) {
        return responseEvent.getResponse().getStatusLine().getStatusCode() == 200;
    }
}
