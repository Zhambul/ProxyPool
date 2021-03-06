package test.database;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import test.entity.Proxy;
import test.util.HibernateSessionFactory;

import java.util.List;

/**
 * Created by 10 on 18.04.2016.
 */
public class ProxyRepositoryImpl implements ProxyRepository{

    public void saveOrUpdateAll(List<Proxy> proxies) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();

        for (Proxy proxy : proxies) {
            session.saveOrUpdate(proxy);
        }

        session.getTransaction().commit();
        session.close();
    }

    public void saveOrUpdate(Proxy proxy) {
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

    public List<Proxy> getAllSorted() {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        SQLQuery sqlQuery = session.createSQLQuery("SELECT * FROM proxy_table ORDER BY is_active DESC, rating DESC");
        sqlQuery.addEntity(Proxy.class);
        return sqlQuery.list();
    }

    public List<Proxy> getAll() {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        List res = session.createCriteria(Proxy.class).list();
        return res;
    }

    @Override
    public List<Proxy> getActive() {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        SQLQuery sqlQuery = session.createSQLQuery("SELECT * FROM proxy_table" +
                                                    " WHERE is_active = TRUE ");
        sqlQuery.addEntity(Proxy.class);
        return sqlQuery.list();
    }

    @Override
    public List<Proxy> getHTTPSOnly() {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        SQLQuery sqlQuery = session.createSQLQuery("SELECT * FROM proxy_table" +
                                                   " WHERE scheme = 'HTTPS' ");
        sqlQuery.addEntity(Proxy.class);
        return sqlQuery.list();
    }

    @Override
    public List<Proxy> getChinaOnly() {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        SQLQuery sqlQuery = session.createSQLQuery("SELECT * FROM proxy_table" +
                                                   " WHERE country = 'China' ");
        sqlQuery.addEntity(Proxy.class);
        return sqlQuery.list();
    }
}
