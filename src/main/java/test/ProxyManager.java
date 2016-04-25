package test;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import akka.routing.RoundRobinPool;
import test.database.ProxyRepository;
import test.entity.Proxy;
import test.event.*;
import test.validation.Validation200InResponse;
import test.validation.ValidationNullResponse;
import test.validation.ValidationStrategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Жамбыл on 4/23/2016.
 */
class ProxyManager extends UntypedActor{
    private LoggingAdapter logger = Logging.getLogger(getContext().system(),this);

    private static final int nOfRequests = 10;

    private final ProxyRepository proxyRepository;
    private ActorRef proxyChecker;
    private ActorRef proxyRequest;
    private ActorRef proxyParser;
    private ActorRef server;
    private List<ValidationStrategy> validators;

    ProxyManager(ProxyRepository proxyRepository) {
        this.proxyRepository = proxyRepository;

        proxyRequest = getContext().actorOf(
                Props.create(ProxyRequest.class, (Creator<ProxyRequest>) ProxyRequest::new)
                        .withRouter(new RoundRobinPool(nOfRequests)));

        proxyParser = getContext().actorOf(Props.create(ProxyParser.class));
        validators = new ArrayList<>();
        validators.add(new ValidationNullResponse(getContext().system()));
        validators.add(new Validation200InResponse(getContext().system()));
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof StartCheckEvent) {
            StartCheckEvent startCheckEvent = (StartCheckEvent) o;
            startChecking(startCheckEvent);
        }
        else if(o.equals("stopChecking")){
            stopChecking();
        }
        else if(o instanceof ExecuteRequestEvent) {
            ExecuteRequestEvent executeRequestEvent = (ExecuteRequestEvent) o;
            executeProxyRequest(executeRequestEvent);
            if(!getSelf().equals(getSender())) {
                server = getSender();
            }
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

    private void executeProxyRequest(ExecuteRequestEvent executeRequestEvent) {
        String targetUrl = validateUrl(executeRequestEvent.getUrl());
        List<Proxy> proxiesActive = proxyRepository.getActive();
        if(proxiesActive.size() == 0) {
            logger.info("no active proxies ");
            return;
        }
        Proxy proxy = proxiesActive.get(new Random().nextInt(proxiesActive.size()));
        ProxyRequestEvent proxyRequestEvent = new ProxyRequestEvent(proxy, targetUrl,executeRequestEvent.getTimeOut(),executeRequestEvent.getRequestId());
        proxyRequest.tell(proxyRequestEvent,getSelf());
    }

    private void onProxyResponse(ProxyResponseEvent proxyResponse) throws IOException {
        for (ValidationStrategy validator : validators) {
            if(!validator.validate(proxyResponse)) {
                onProxyFailed(proxyResponse);
                return;
            }
        }
        logger.info(proxyResponse.getProxy().getCountry() + " is done");
        server.tell(proxyResponse,getSelf());
    }

    private void onProxyFailed(ProxyResponseEvent proxyResponse) {
        logger.info("fail using proxy with id " + proxyResponse.getProxy().getId() + " from "
                                                + proxyResponse.getProxy().getCountry() +", try next ");
        Proxy failedProxy = proxyResponse.getProxy();
        failedProxy.decRating();
        failedProxy.setActive(false);
        proxyRepository.update(failedProxy);
        ExecuteRequestEvent executeRequestEvent = new ExecuteRequestEvent(proxyResponse.getUrl(),proxyResponse.getTimeOut(),proxyResponse.getRequestId());
        getSelf().tell(executeRequestEvent, getSelf());
    }

    private void startChecking(StartCheckEvent startCheckEvent) {
        if(proxyChecker == null) {
            proxyChecker = getContext().actorOf(
                    Props.create(ProxyChecker.class, (Creator<ProxyChecker>) () ->
                            new ProxyChecker(proxyRepository)), "checker");

            proxyChecker.tell(startCheckEvent,getSelf());
        }
        else {
            logger.info("already checking");
        }
    }

    private void stopChecking() {
        proxyChecker.tell(PoisonPill.getInstance(),getSelf());
        proxyChecker = null;
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
