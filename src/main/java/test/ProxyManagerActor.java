package test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import test.database.ProxyRepository;
import test.entity.Proxy;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Жамбыл on 4/23/2016.
 */
public class ProxyManagerActor extends UntypedActor{
    private LoggingAdapter logger = Logging.getLogger(getContext().system(),this);
    private final ProxyRepository proxyRepository;

    private static final int CHECkING_TIMEOUT = 2 * 1000;
    private ProxyRequest proxyRequest;

    public ProxyManagerActor(ProxyRepository proxyRepository) {
        this.proxyRepository = proxyRepository;
        proxyRequest = new ProxyRequest();
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof String) {
            String message = (String) o;
            if(message.equals("startMonitoring")) {
                startMonitoring();
            }
            else if(message.contains("executeProxyRequest ")) {
                executeProxyRequest(message);
            }
        }
        else {
            unhandled(o);
        }
    }

    private void executeProxyRequest(String message) {
        String url = message.replace("executeProxyRequest ","");
        String targetUrl = validateUrl(url);
        List<Proxy> proxiesSorted = proxyRepository.getAllSorted();
        Proxy bestProxy = proxiesSorted.get(0);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            CloseableHttpResponse response = proxyRequest.execute(bestProxy, 5 * 1000, httpClient, targetUrl);
            logger.info(EntityUtils.toString(response.getEntity(), "UTF-8"));
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

    private void startMonitoring() {
        List<Proxy> proxies = proxyRepository.getAllSorted();
        for (Proxy proxy : proxies) {
            ActorRef proxyChecker = getContext().actorOf(
                    Props.create(ProxyCheckerActor.class,(Creator<ProxyCheckerActor>) () ->
                            new ProxyCheckerActor(proxy,CHECkING_TIMEOUT,proxyRepository)));

            proxyChecker.tell("check",getSelf());
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
}
