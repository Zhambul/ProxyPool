package test;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import akka.routing.RoundRobinPool;
import org.apache.http.client.methods.CloseableHttpResponse;
import test.database.ProxyRepository;
import test.entity.Proxy;
import test.event.ProxyRequestEvent;
import test.event.ProxyResponseEvent;

import java.sql.Date;
import java.util.Calendar;

/**
 * Created by Жамбыл on 4/23/2016.
 */
class ProxyChecker extends UntypedActor {

    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);;

    private int timeOut;
    private ProxyRepository proxyRepository;

    private ActorRef proxyRequest;
    private final static int nOfRequests = 10;

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
        if(o instanceof Proxy) {
            Proxy proxy = (Proxy) o;
            ProxyRequestEvent proxyRequestEvent = new ProxyRequestEvent(proxy,"www.google.com",timeOut);
            proxyRequest.tell(proxyRequestEvent,getSelf());
        }
        else if(o instanceof ProxyResponseEvent) {
            ProxyResponseEvent proxyResponse = (ProxyResponseEvent) o;
            validateResponse(proxyResponse.getProxy(), proxyResponse.getResponse());

            if(proxyResponse.getResponse() != null) {
                proxyResponse.getResponse().close();
            }
        }
        else {
            logger.info(o.toString());
            unhandled(o);
        }
    }
    private void validateResponse(Proxy proxy, CloseableHttpResponse response) {
        if(response!= null && response.getStatusLine().getStatusCode() == 200) {
            onProxySucceeded(proxy);
        }
        else {
            logger.debug(proxy.getIp() + " "+ proxy.getScheme());
            onProxyFailed(proxy);
        }
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
