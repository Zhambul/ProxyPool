package test;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.Creator;
import akka.routing.RoundRobinPool;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.log4j.Logger;
import test.database.ProxyRepository;
import test.database.ProxyRepositoryImpl;
import test.entity.Proxy;

import java.util.List;


public class Main {

    static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
        ProxyParser proxyParser;
        ProxyRepository proxyRepository = new ProxyRepositoryImpl();
        ProxyManager proxyManager = new ProxyManager(proxyRepository);
        OptionParser parser = new OptionParser() {
            {
                accepts("request").withRequiredArg();
                accepts("check");
                accepts("parse").withRequiredArg();
            }
        };

        OptionSet optionSet = parser.parse(args);

        if (optionSet.has("request")) {
            String url = (String) optionSet.valueOf("request");
            logger.info("request to " + url);
            proxyManager.executeProxyRequest(url);
        }

        if (optionSet.has("check")) {
            logger.info("checking");

            ActorSystem system = ActorSystem.create("system");
            ActorRef proxyManagerRef = system.actorOf(
                    Props.create(ProxyManagerActor.class,(Creator<ProxyManagerActor>) () ->
                            new ProxyManagerActor(proxyRepository)).withRouter(new RoundRobinPool(1)));
            proxyManagerRef.tell("startMonitoring",ActorRef.noSender());

        } else if (optionSet.has("parse")) {
            String filePath = (String) optionSet.valueOf("parse");
            logger.info("parsing " + filePath );
            proxyParser = new ProxyParser(filePath);
            List<Proxy> parsedProxies = proxyParser.parse();
            proxyRepository.saveOrUpdateAll(parsedProxies);
            logger.info("success");
        }
    }
}
