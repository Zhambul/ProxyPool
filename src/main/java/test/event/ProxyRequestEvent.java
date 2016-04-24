package test.event;

import test.entity.Proxy;

/**
 * Created by Жамбыл on 4/23/2016.
 */
public class ProxyRequestEvent {
    private final Proxy proxy;
    private final String url;
    private int timeOut;

    public ProxyRequestEvent(Proxy proxy, String url,int timeOut) {
        this.proxy = proxy;
        this.url = url;
        this.timeOut = timeOut;
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
