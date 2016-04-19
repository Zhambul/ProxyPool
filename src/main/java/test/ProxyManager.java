package test;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import test.database.ProxyRepository;
import test.entity.Proxy;
import test.util.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 10 on 18.04.2016.
 */
class ProxyManager {

    private final ProxyRepository proxyRepository;
    private ExecutorService executorService;

    private static final int CHECkING_TIMEOUT = 2 * 1000;
    private boolean isWorking;
    private ProxyRequest proxyRequest;

    ProxyManager(ProxyRepository proxyRepository) {
        this.proxyRepository = proxyRepository;
        executorService = Executors.newFixedThreadPool(10);
        proxyRequest = new ProxyRequest();
    }

    void executeProxyRequest(String url) {
        String targetUrl = validateUrl(url);
        List<Proxy> proxiesSorted = proxyRepository.getAllSorted();
        Proxy bestProxy = proxiesSorted.get(0);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            CloseableHttpResponse response = proxyRequest.execute(bestProxy, 5 * 1000, httpClient, targetUrl);
            Logger.i(EntityUtils.toString(response.getEntity(), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String validateUrl(String url) {
        String targetUrl = url;
        if(url.toLowerCase().contains("https")) {
            targetUrl = url.replace("https://", "");
        }
        else if(url.toLowerCase().contains("http")){
            targetUrl = url.replace("http://", "");
        }
        return targetUrl;
    }

    void startMonitoring() {
        if(!isWorking) {
            isWorking = true;
            List<Proxy> proxies = proxyRepository.getAllSorted();
            for (Proxy proxy : proxies) {
                executorService.submit(new ProxyChecker(proxy, CHECkING_TIMEOUT, proxyRepository));
            }
            isWorking = false;
        }
    }

    public void stopMonitoring() {
        isWorking = false;
        executorService.shutdown();
    }
}
