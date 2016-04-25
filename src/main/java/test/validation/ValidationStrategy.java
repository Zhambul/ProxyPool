package test.validation;

import test.event.ProxyResponseEvent;

/**
 * Created by user on 25.04.2016.
 */
public interface ValidationStrategy {

    boolean validate(ProxyResponseEvent responseEvent);
}
