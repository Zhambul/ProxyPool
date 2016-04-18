package test.database;

import test.entity.Proxy;

import java.util.List;

/**
 * Created by 10 on 18.04.2016.
 */
public interface ProxyRepository {

    void saveAll(List<Proxy> proxies);

    void save(Proxy proxy);

    void delete(Proxy proxy);

    void update(Proxy proxy);

    List<Proxy> getAll();
}
