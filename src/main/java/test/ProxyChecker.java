package test;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import test.database.ProxyRepository;
import test.entity.Proxy;
import test.util.Logger;

import java.io.IOException;
import java.sql.Date;
import java.util.Calendar;

/**
 * Created by 10 on 19.04.2016.
 */
class ProxyChecker implements Runnable {

    private Proxy proxyEntity;
    private int timeOut;
    private ProxyRequest proxyRequest;
    private ProxyRepository proxyRepository;


    ProxyChecker(Proxy proxy, int timeOut, ProxyRepository proxyRepository) {
        this.proxyEntity = proxy;
        this.timeOut = timeOut;
        this.proxyRepository = proxyRepository;
        proxyRequest = new ProxyRequest();
    }

    public void run() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            proxyEntity.setLastRequestDate(new Date(Calendar.getInstance().getTimeInMillis()));
            proxyRepository.update(proxyEntity);
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

    private void validateResponse(Proxy proxyEntity, CloseableHttpResponse response) {
        if(response.getStatusLine().getStatusCode() == 200) {
            onProxySucceeded(proxyEntity);
        }
        else {
            onProxyFailed(proxyEntity);
        }
    }

    private void onProxySucceeded(Proxy proxyEntity) {
        Logger.i("proxy with id " + proxyEntity.getId() + " is active");
        proxyEntity.incRating();
        proxyEntity.setActive(true);
        proxyRepository.update(proxyEntity);
    }

    private void onProxyFailed(Proxy proxyEntity) {
        proxyEntity.decRating();
        proxyEntity.setActive(false);
        proxyRepository.update(proxyEntity);
    }

}
