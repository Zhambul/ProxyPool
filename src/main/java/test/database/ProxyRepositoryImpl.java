package test.database;

import org.hibernate.Session;
import test.entity.Proxy;
import test.util.HibernateSessionFactory;

import java.util.List;

/**
 * Created by 10 on 18.04.2016.
 */
public class ProxyRepositoryImpl implements ProxyRepository{

    public void saveAll(List<Proxy> proxies) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();

        for (Proxy proxy : proxies) {
            session.saveOrUpdate(proxy);
        }

        session.getTransaction().commit();
        session.close();
    }

    public void save(Proxy proxy) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();
        session.saveOrUpdate(proxy);
        session.getTransaction().commit();
        session.close();
    }

    public void delete(Proxy proxy) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();
        session.delete(proxy);
        session.getTransaction().commit();
        session.close();
    }

    public void update(Proxy proxy) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();
        session.update(proxy);
        session.getTransaction().commit();
        session.close();
    }

    public List<Proxy> getAll() {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        return session.createCriteria(Proxy.class).list();
    }
}
