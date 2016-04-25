package test;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import test.entity.Proxy;
import test.event.ProxyRequestEvent;
import test.event.ProxyResponseEvent;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * Created by 10 on 19.04.2016.
 */
class ProxyRequest extends UntypedActor {

    private CloseableHttpClient httpClient;

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(),this);

    public ProxyRequest() {
        httpClient = HttpClients.createDefault();
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof ProxyRequestEvent) {
            ProxyRequestEvent proxyRequestEvent = (ProxyRequestEvent) o;
            Proxy proxy = proxyRequestEvent.getProxy();
            String targetUrl = proxyRequestEvent.getUrl();
            int timeOut = proxyRequestEvent.getTimeOut();
            int requestId = proxyRequestEvent.getRequestId();

            ProxyResponseEvent proxyResponseEvent = executeRequest(proxy,targetUrl,timeOut,requestId);
            getSender().tell(proxyResponseEvent, getSelf());
        }else {
            unhandled(o);
        }
    }

    private ProxyResponseEvent executeRequest(Proxy proxy, String targetUrl, int timeOut, int requestId) throws IOException {

        HttpHost proxyHost = new HttpHost(proxy.getIp(), proxy.getPort(), proxy.getScheme());

        HttpHost target;
        if(proxy.getScheme().toLowerCase().equals("https")) {
            target = new HttpHost(targetUrl);
        }
        else {
            target = new HttpHost(targetUrl,80,proxy.getScheme());
        }

        RequestConfig config = RequestConfig.custom()
                .setProxy(proxyHost)
                .build();

        HttpGet request = new HttpGet("/");
        request.setConfig(config);

        logger.debug("Executing request " + request.getRequestLine() + " to " + target + " via " + proxyHost);

        Future<CloseableHttpResponse> result =
                Executors.newSingleThreadExecutor().submit(() ->
                    httpClient.execute(target, request));

        CloseableHttpResponse response = null;
        try {
            response = result.get(timeOut,TimeUnit.MILLISECONDS);
            logger.debug("request via proxy with id "+ proxy.getId() +" executed with code " + response.getStatusLine().getStatusCode() );
        } catch (InterruptedException  e) {
            logger.debug("interrupted exception via proxy with id " + proxy.getId());
        }catch (ExecutionException e) {
            logger.debug("error during request via proxy with id " + proxy.getId());
        } catch (TimeoutException e) {
            logger.debug("timeout exception ("+timeOut+" milliseconds) via proxy with id " + proxy.getId());
        }

        return new ProxyResponseEvent(proxy,response, targetUrl,timeOut,requestId);
    }

    private void test() {

    }
}
