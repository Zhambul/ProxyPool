package test;

import com.sun.deploy.net.proxy.ProxyType;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import test.database.ProxyRepository;
import test.entity.Proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by 10 on 18.04.2016.
 */
class ProxyManager {

    private final ProxyRepository proxyRepository;
    private ExecutorService executorService;

    private boolean isWorking;
    private int sleepTimeInSeconds = 5;

    ProxyManager(ProxyRepository proxyRepository) {
        this.proxyRepository = proxyRepository;
        executorService = Executors.newFixedThreadPool(10);
    }

    void startMonitoring() {
        if(!isWorking) {
            isWorking = true;
            List<Proxy> proxies = proxyRepository.getAll();
            for (Proxy proxy : proxies) {
                executorService.submit(new ProxyChecker(proxy));
            }
            isWorking = false;
        }
    }

    public void stopMonitoring() {
        isWorking = false;
        executorService.shutdown();
    }

    private class ProxyChecker implements Runnable {

        Proxy proxy;

        ProxyChecker(Proxy proxy) {
            this.proxy = proxy;
        }

        public void run() {
            request(proxy,7*1000);
        }

        private void request(Proxy proxyEntity, int timeOut) {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            try {
                HttpHost proxy = new HttpHost(proxyEntity.getIp(), proxyEntity.getPort(), proxyEntity.getScheme());
                HttpHost target;
                if(proxyEntity.getScheme().equals("https")) {
                    target = new HttpHost("google.com",443,"https");
                }
                else {
                    target = new HttpHost("google.com");
                }

                RequestConfig config = RequestConfig.custom()
                        .setConnectTimeout(timeOut)
                        .setProxy(proxy)
                        .build();
                HttpGet request = new HttpGet("/");
                request.setConfig(config);

                System.out.println("Executing request " + request.getRequestLine() + " to " + target + " via " + proxy);

                CloseableHttpResponse response = httpclient.execute(target, request);
                try {
                    if(response.getStatusLine().getStatusCode() == 200) {
                        System.out.println("----------------------------------------");
                        System.out.println("proxy with id " + proxyEntity.getId() + " is valid");
                        proxyEntity.setActive(true);
                        proxyRepository.update(proxyEntity);
                    }
                    else {
                        proxyEntity.setActive(false);
                    }
                } finally {
                    response.close();
                }
            } catch (IOException e) {
//                e.printStackTrace();
            } finally {
                try {
                    httpclient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
