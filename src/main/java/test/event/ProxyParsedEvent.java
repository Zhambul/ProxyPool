package test.event;

import test.entity.Proxy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Жамбыл on 4/23/2016.
 */
public class ProxyParsedEvent {
    private final List<Proxy> proxies;

    public ProxyParsedEvent(List<Proxy> proxies) {
        this.proxies = proxies;
    }

    public List<Proxy> getProxies() {
        return new ArrayList<>(proxies);
    }
}
