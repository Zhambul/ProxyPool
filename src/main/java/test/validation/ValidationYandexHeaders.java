package test.validation;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import test.event.ProxyResponseEvent;

/**
 * Created by user on 25.04.2016.
 */
public class ValidationYandexHeaders implements ValidationStrategy {

    @Override
    public boolean validate(ProxyResponseEvent responseEvent) {
        if(!responseEvent.getUrl().contains("yandex.ru")){
            return true;
        }
        CloseableHttpResponse response = responseEvent.getResponse();
        Header[] headerToCheck = response.getHeaders("x-frame-options");
        return !(headerToCheck == null || headerToCheck.length == 0);
    }
}
