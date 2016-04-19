package test;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import test.entity.Proxy;
import test.util.Logger;

import java.io.IOException;
/**
 * Created by 10 on 19.04.2016.
 */
class ProxyRequest {



    CloseableHttpResponse execute(Proxy proxyEntity, int timeOut, CloseableHttpClient httpclient, String targetUrl)
            throws IOException {
        HttpHost proxy = new HttpHost(proxyEntity.getIp(), proxyEntity.getPort(), proxyEntity.getScheme());

        int targetPort = proxyEntity.getScheme().toLowerCase().equals("https") ? 443 : 80;
        HttpHost target = new HttpHost(targetUrl,targetPort,proxyEntity.getScheme());

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeOut)
                .setProxy(proxy)
                .build();
        HttpGet request = new HttpGet("/");
        request.setConfig(config);

        Logger.d("Executing request " + request.getRequestLine() + " to " + target + " via " + proxy);

        return httpclient.execute(target, request);
    }
}
