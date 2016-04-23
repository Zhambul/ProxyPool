package test;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import test.database.ProxyRepository;
import test.entity.Proxy;

import java.io.IOException;
import java.sql.Date;
import java.util.Calendar;

/**
 * Created by Жамбыл on 4/23/2016.
 */
public class ProxyCheckerActor extends UntypedActor {

    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private Proxy proxyEntity;
    private int timeOut;
    private ProxyRequest proxyRequest;
    private ProxyRepository proxyRepository;

    ProxyCheckerActor(Proxy proxy, int timeOut, ProxyRepository proxyRepository) {
        this.proxyEntity = proxy;
        this.timeOut = timeOut;
        this.proxyRepository = proxyRepository;
        proxyRequest = new ProxyRequest();
    }
    public ProxyCheckerActor() {
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof String && o.equals("check")) {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            try {
                CloseableHttpResponse response = proxyRequest.execute(proxyEntity, timeOut, httpclient, "google.com");
                try {
                    validateResponse(proxyEntity, response);
                } finally {
                    response.close();
                }
            }
            catch (Exception e) {
                onProxyFailed(proxyEntity);
            } finally {
                try {
                    httpclient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void validateResponse(Proxy proxyEntity, CloseableHttpResponse response) {
        if(response.getStatusLine().getStatusCode() == 200) {
            onProxySucceeded(proxyEntity);
        }
        else {
            onProxyFailed(proxyEntity);
        }
    }

    private void onProxySucceeded(Proxy proxyEntity) {
        logger.info("proxy with id " + proxyEntity.getId() + " is active");
        proxyEntity.setLastRequestDate(new Date(Calendar.getInstance().getTimeInMillis()));
        proxyEntity.incRating();
        proxyEntity.setActive(true);
        proxyRepository.update(proxyEntity);
    }

    private void onProxyFailed(Proxy proxyEntity) {
        proxyEntity.setLastRequestDate(new Date(Calendar.getInstance().getTimeInMillis()));
        proxyEntity.decRating();
        proxyEntity.setActive(false);
        proxyRepository.update(proxyEntity);
    }

}
