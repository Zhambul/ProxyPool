package test.event;

import com.sun.istack.internal.Nullable;
import org.apache.http.client.methods.CloseableHttpResponse;
import test.entity.Proxy;

/**
 * Created by Жамбыл on 4/23/2016.
 */
public class ProxyResponseEvent {
    private final Proxy proxy;
    private final CloseableHttpResponse response;
    private final String url;
    private final int timeOut;

    public ProxyResponseEvent(Proxy proxy, CloseableHttpResponse response, String url, int timeOut) {
        this.proxy = proxy;
        this.response = response;
        this.url = url;
        this.timeOut = timeOut;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public Proxy getProxy() {
        return proxy;
    }

    @Nullable
    public CloseableHttpResponse getResponse() {
        return response;
    }

    public String getUrl() {
        return url;
    }
}
