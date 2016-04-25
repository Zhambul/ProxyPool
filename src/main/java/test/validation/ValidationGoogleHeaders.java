package test.validation;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import test.event.ProxyResponseEvent;

/**
 * Created by user on 25.04.2016.
 */
public class ValidationGoogleHeaders implements ValidationStrategy {

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
        return !(headerToCheck == null || headerToCheck.length == 0);
    }
}
