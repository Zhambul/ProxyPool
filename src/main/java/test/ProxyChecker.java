package test;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import akka.routing.RoundRobinPool;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import test.database.ProxyRepository;
import test.entity.Proxy;
import test.event.CheckProxiesEvent;
import test.event.ProxyRequestEvent;
import test.event.ProxyResponseEvent;

import java.io.IOException;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Жамбыл on 4/23/2016.
 */
class ProxyChecker extends UntypedActor {

    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);;

    private int timeOut;
    private ProxyRepository proxyRepository;
    private ActorRef proxyRequest;
    private final static int nOfRequests = 10;
    private int currentNumberOfResponses;
    private List<Proxy> proxiesToCheck;
    private ActorRef proxyManager;

    @Override
    public void preStart() throws Exception {
        proxyRequest = getContext().actorOf(Props.create(ProxyRequest.class,
                (Creator<ProxyRequest>) ProxyRequest::new).withRouter(new RoundRobinPool(nOfRequests)));
    }

    ProxyChecker(int timeOut, ProxyRepository proxyRepository) {
        this.timeOut = timeOut;
        this.proxyRepository = proxyRepository;
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof CheckProxiesEvent) {
            onCheckProxies((CheckProxiesEvent) o);
        }
        else if(o instanceof ProxyResponseEvent) {
            onProxyResponse((ProxyResponseEvent) o);
        }
        else {
            unhandled(o);
        }
    }

    private void onCheckProxies(CheckProxiesEvent checkProxiesEvent) {
        proxyManager = getSender();
        proxiesToCheck = checkProxiesEvent.getProxies();
        proxiesToCheck.forEach(proxy -> {
            String targetUrl = "www.google.com";
            int dummyRequestId = -1;
            if(proxy.getCountry().equals("China")) {
                targetUrl = "www.renren.com";
            }
            ProxyRequestEvent proxyRequestEvent = new ProxyRequestEvent(proxy,targetUrl,timeOut,dummyRequestId);
            proxyRequest.tell(proxyRequestEvent,getSelf());
        });
    }

    private void onProxyResponse(ProxyResponseEvent proxyResponseEvent) throws IOException {
        currentNumberOfResponses++;
        if(currentNumberOfResponses == proxiesToCheck.size()) {
            logger.info("stopChecking");
            proxyManager.tell("stopChecking",getSelf());
        }
        validateResponse(proxyResponseEvent.getProxy(), proxyResponseEvent.getResponse());

        if(proxyResponseEvent.getResponse() != null) {
            proxyResponseEvent.getResponse().close();
        }
    }

    private void validateResponse(Proxy proxy, CloseableHttpResponse response) {
        if(validateProxyResponse(response)) {
            onProxySucceeded(proxy);
        }
        else {
            logger.debug(proxy.getIp() + " "+ proxy.getScheme());
            onProxyFailed(proxy);
        }
    }

    private boolean validateProxyResponse(CloseableHttpResponse response) {
        if(response == null) {
            return false;
        }
        Header[] xFrameOptionHeader = response.getHeaders("x-frame-options");
        if(xFrameOptionHeader == null ||xFrameOptionHeader.length==0)
        {
            logger.debug("missing header ");
            return false;
        }
        return response.getStatusLine().getStatusCode() == 200 && xFrameOptionHeader[0].getValue().length() != 0;
    }

    private void onProxySucceeded(Proxy proxy) {
        logger.info("proxy with id " + proxy.getId() + " is active");
        proxy.setLastRequestDate(new Date(Calendar.getInstance().getTimeInMillis()));
        proxy.incRating();
        proxy.setActive(true);
        proxyRepository.update(proxy);
    }

    private void onProxyFailed(Proxy proxyEntity) {
        logger.debug("proxy with id " + proxyEntity.getId() + " failed");
        proxyEntity.setLastRequestDate(new Date(Calendar.getInstance().getTimeInMillis()));
        proxyEntity.decRating();
        proxyEntity.setActive(false);
        proxyRepository.update(proxyEntity);
    }

}
