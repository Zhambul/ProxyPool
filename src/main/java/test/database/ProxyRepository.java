package test.database;

import test.entity.Proxy;

import java.util.List;

/**
 * Created by 10 on 18.04.2016.
 */
public interface ProxyRepository {

    void saveOrUpdateAll(List<Proxy> proxies);

    void saveOrUpdate(Proxy proxy);

    void delete(Proxy proxy);

    void update(Proxy proxy);

    List<Proxy> getAllSorted();

    List<Proxy> getAll();

    List<Proxy> getActive();
}
