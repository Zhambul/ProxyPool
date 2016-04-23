package test.event;

import test.entity.Proxy;

/**
 * Created by Жамбыл on 4/23/2016.
 */
public class ProxyRequestEvent {
    private final Proxy proxy;
    private final String url;

    public ProxyRequestEvent(Proxy proxy, String url) {
        this.proxy = proxy;
        this.url = url;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public String getUrl() {
        return url;
    }
}
