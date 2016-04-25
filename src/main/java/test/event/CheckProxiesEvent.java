package test.event;

import test.entity.Proxy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Жамбыл on 4/25/2016.
 */
public class CheckProxiesEvent {
    private final List<Proxy> proxies;

    public CheckProxiesEvent(List<Proxy> proxies) {
        this.proxies = proxies;
    }

    public List<Proxy> getProxies() {
        return new ArrayList<>(proxies);
    }
}
