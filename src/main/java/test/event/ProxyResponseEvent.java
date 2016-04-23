package test.event;

import com.sun.istack.internal.Nullable;
import org.apache.http.client.methods.CloseableHttpResponse;
import test.entity.Proxy;

/**
 * Created by Жамбыл on 4/23/2016.
 */
public class ProxyResponseEvent {
    private Proxy proxy;
    private CloseableHttpResponse response;

    public ProxyResponseEvent(Proxy proxy, CloseableHttpResponse response) {
        this.proxy = proxy;
        this.response = response;
    }

    public Proxy getProxy() {
        return proxy;
    }

    @Nullable
    public CloseableHttpResponse getResponse() {
        return response;
    }
}
