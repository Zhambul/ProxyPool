package test.event;

import test.entity.Proxy;

/**
 * Created by Жамбыл on 4/23/2016.
 */
public class ProxyRequestEvent {
    private final Proxy proxy;
    private final String url;
    private final int timeOut;
    private final int requestId;

    public ProxyRequestEvent(Proxy proxy, String url,int timeOut,int requestId) {
        this.proxy = proxy;
        this.url = url;
        this.timeOut = timeOut;
        this.requestId = requestId;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public String getUrl() {
        return url;
    }
}
