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
import test.event.ProxyParseEvent;
import test.event.ProxyParsedEvent;
import test.event.ProxyRequestEvent;
import test.event.ProxyResponseEvent;

import java.util.List;

/**
 * Created by Жамбыл on 4/23/2016.
 */
class ProxyManager extends UntypedActor{
    private LoggingAdapter logger = Logging.getLogger(getContext().system(),this);

    private static final int nOfRequests = 10;
    private static final int CHECKING_TIMEOUT = 3 * 1000;
    private static final int REQUEST_TIMEOUT = 5 * 1000;

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
        if(o instanceof String) {
            String message = (String) o;
            if(message.equals("startMonitoring")) {
                startMonitoring();
            }
            else if(message.contains("executeProxyRequest ")) {
                executeProxyRequest(message);
            }
        }
        else if(o instanceof ProxyResponseEvent) {
            ProxyResponseEvent proxyResponse = (ProxyResponseEvent) o;
            logger.info(proxyResponse.getResponse().getStatusLine().toString());
            proxyResponse.getResponse().close();
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

    private void executeProxyRequest(String message) {
        String url = message.replace("executeProxyRequest ","");
        String targetUrl = validateUrl(url);
        List<Proxy> proxiesSorted = proxyRepository.getAllSorted();
        Proxy bestProxy = proxiesSorted.get(0);
        ProxyRequestEvent proxyRequestEvent = new ProxyRequestEvent(bestProxy, targetUrl);
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
