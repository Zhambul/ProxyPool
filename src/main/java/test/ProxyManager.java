package test;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import akka.routing.RoundRobinPool;
import test.database.ProxyRepository;
import test.entity.Proxy;
import test.event.*;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Created by Жамбыл on 4/23/2016.
 */
class ProxyManager extends UntypedActor{
    private LoggingAdapter logger = Logging.getLogger(getContext().system(),this);

    private static final int nOfRequests = 10;
    private static final int CHECKING_TIMEOUT = 3 * 1000;
    private static final int REQUEST_TIMEOUT = 3 * 1000;

    private final ProxyRepository proxyRepository;
    private ActorRef proxyChecker;
    private ActorRef proxyRequest;
    private ActorRef proxyParser;

    ProxyManager(ProxyRepository proxyRepository) {
        this.proxyRepository = proxyRepository;

        proxyChecker = getContext().actorOf(
                Props.create(ProxyChecker.class,(Creator<ProxyChecker>) () ->
                        new ProxyChecker(CHECKING_TIMEOUT,proxyRepository)));

        proxyRequest = getContext().actorOf(
                Props.create(ProxyRequest.class, (Creator<ProxyRequest>) ()
                    -> new ProxyRequest(REQUEST_TIMEOUT)).withRouter(new RoundRobinPool(nOfRequests)));

        proxyParser = getContext().actorOf(Props.create(ProxyParser.class));
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof String && o.equals("startMonitoring")) {
            startMonitoring();
        }
        else if(o instanceof ExecuteRequestEvent) {
            ExecuteRequestEvent executeRequestEvent = (ExecuteRequestEvent) o;
            executeProxyRequest(executeRequestEvent.getUrl());
        }
        else if(o instanceof ProxyResponseEvent) {
            ProxyResponseEvent proxyResponse = (ProxyResponseEvent) o;
            onProxyResponse(proxyResponse);
        }
        else if(o instanceof ProxyParseEvent) {
            ProxyParseEvent proxyParseEvent = (ProxyParseEvent) o;
            proxyParser.tell(proxyParseEvent.getFilePath(),getSelf());
        }
        else if(o instanceof ProxyParsedEvent) {
            ProxyParsedEvent proxyParsedEvent = (ProxyParsedEvent) o;
            proxyRepository.saveOrUpdateAll(proxyParsedEvent.getProxies());
            logger.info("successfully parsed and added to database");
        }
        else {
            unhandled(o);
        }
    }

    private void onProxyResponse(ProxyResponseEvent proxyResponse) throws IOException {
        if(proxyResponse.getResponse() == null || proxyResponse.getResponse().getStatusLine().getStatusCode() != 200) {
            logger.info("fail using proxy, try next ");
            Proxy failedProxy = proxyResponse.getProxy();
            failedProxy.decRating();
            failedProxy.setActive(false);
            logger.info("updating ");
            proxyRepository.update(failedProxy);
            getSelf().tell("executeProxyRequest " + proxyResponse.getUrl(), getSelf());
        }
        else {
            logger.info(proxyResponse.getResponse().getStatusLine().toString());
            proxyResponse.getResponse().close();
        }
    }

    private void executeProxyRequest(String url) {
        logger.info("exec command");
        String targetUrl = validateUrl(url);
        List<Proxy> proxiesActive = proxyRepository.getActive();
        if(proxiesActive.size() == 0) {
            logger.info("no active proxies ");
            return;
        }
        Proxy proxy = proxiesActive.get(new Random().nextInt(proxiesActive.size()));
        logger.info("executing request via proxy with id " + proxy.getId() +" to " + targetUrl);
        ProxyRequestEvent proxyRequestEvent = new ProxyRequestEvent(proxy, targetUrl);
        proxyRequest.tell(proxyRequestEvent,getSelf());
    }

    private void startMonitoring() {
        List<Proxy> proxies = proxyRepository.getAllSorted();
        for (Proxy proxy : proxies) {
            proxyChecker.tell(proxy, getSelf());
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
