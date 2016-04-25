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
import test.event.ProxyRequestEvent;
import test.event.ProxyResponseEvent;
import test.event.StartCheckEvent;
import test.validation.*;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Жамбыл on 4/23/2016.
 */
class ProxyChecker extends UntypedActor {

    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);;

    private ProxyRepository proxyRepository;
    private ActorRef proxyRequest;
    private final static int nOfRequests = 10;
    private int currentNumberOfResponses;
    private List<Proxy> proxiesToCheck;
    private ActorRef proxyManager;
    private List<ValidationStrategy> validators;

    @Override
    public void preStart() throws Exception {
        proxyRequest = getContext().actorOf(Props.create(ProxyRequest.class,
                (Creator<ProxyRequest>) ProxyRequest::new).withRouter(new RoundRobinPool(nOfRequests)));
    }

    ProxyChecker(ProxyRepository proxyRepository) {
        this.proxyRepository = proxyRepository;
        proxiesToCheck = proxyRepository.getAllSorted();

        validators = new ArrayList<>();
        validators.add(new ValidationNullResponse(getContext().system()));
        validators.add(new Validation200InResponse(getContext().system()));
        validators.add(new ValidationAnonymityOnAzenvNet(getContext().system()));
        validators.add(new ValidationYandexHeaders(getContext().system()));
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof StartCheckEvent) {
            onStartCheckingProxies((StartCheckEvent) o);
        }
        else if(o instanceof ProxyResponseEvent) {
            onProxyResponse((ProxyResponseEvent) o);
        }
        else {
            unhandled(o);
        }
    }

    private void onStartCheckingProxies(StartCheckEvent startCheckEvent) {
        proxyManager = getSender();
        proxiesToCheck.forEach(proxy -> {
            int dummyRequestId = -1;
            String targetUrl = getTargetUrl(startCheckEvent,proxy);
            ProxyRequestEvent proxyRequestEvent = new ProxyRequestEvent(proxy,targetUrl,startCheckEvent.getTimeOut(),dummyRequestId);
            proxyRequest.tell(proxyRequestEvent,getSelf());
        });
    }

    private String getTargetUrl(StartCheckEvent startCheckEvent, Proxy proxy) {
        if (startCheckEvent.getUrl() == null) {
            String targetUrl = "www.google.com";

            if(proxy.getCountry().equals("China")) {
                targetUrl = "www.yandex.ru";
            }
            return targetUrl;
        }
        return startCheckEvent.getUrl();
    }

    private void onProxyResponse(ProxyResponseEvent proxyResponseEvent) throws IOException {
        validateResponse(proxyResponseEvent);

        if(proxyResponseEvent.getResponse() != null) {
            proxyResponseEvent.getResponse().close();
        }

        stopCheckingIfNeed();
    }

    private void stopCheckingIfNeed() {
        currentNumberOfResponses++;
        if(currentNumberOfResponses == proxiesToCheck.size()) {
            logger.info("stopChecking");
            proxyManager.tell("stopChecking",getSelf());
        }
    }

    private void validateResponse(ProxyResponseEvent proxyResponseEvent) {
        for (ValidationStrategy validator : validators) {
            if(!validator.validate(proxyResponseEvent)) {
                onProxyFailed(proxyResponseEvent.getProxy());
                return;
            }
        }
        onProxySucceeded(proxyResponseEvent.getProxy());
    }


    private void onProxySucceeded(Proxy proxy) {
        logger.info("proxy with id " + proxy.getId() +" from "+ proxy.getCountry() + " is active");
        proxy.setLastRequestDate(new Date(Calendar.getInstance().getTimeInMillis()));
        proxy.incRating();
        proxy.setActive(true);
        proxyRepository.update(proxy);
    }

    private void onProxyFailed(Proxy proxyEntity) {
        if(proxyEntity.getCountry().equals("China")){
            int a =3;
        }
        proxyEntity.setLastRequestDate(new Date(Calendar.getInstance().getTimeInMillis()));
        proxyEntity.decRating();
        proxyEntity.setActive(false);
        proxyRepository.update(proxyEntity);
    }

}
