package test.validation;

import test.event.ProxyResponseEvent;

/**
 * Created by user on 25.04.2016.
 */
public class ValidationNullResponse implements ValidationStrategy {

    @Override
    public boolean validate(ProxyResponseEvent responseEvent) {
        return responseEvent.getResponse() != null;
    }
}
