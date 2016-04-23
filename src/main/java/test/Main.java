package test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.log4j.Logger;
import test.database.ProxyRepository;
import test.database.ProxyRepositoryImpl;
import test.entity.Proxy;

import java.util.List;


public class Main extends UntypedActor{
    private ProxyRepository proxyRepository = new ProxyRepositoryImpl();
    private ActorRef proxyManagerRef;
    private ActorRef proxyParser;
    private final Logger logger = Logger.getLogger(Main.class.getName());

    private void start(String args[]) {
        initActorSystem();
        OptionParser parser = initCLIParser();
        OptionSet optionSet = parser.parse(args);

        if (optionSet.has("request")) {
            String url = (String) optionSet.valueOf("request");
            logger.info("request to " + url);
            proxyManagerRef.tell("executeProxyRequest " + url, getSelf());
        }

        if (optionSet.has("check")) {
            logger.info("checking");
            proxyManagerRef.tell("startMonitoring", getSelf());

        } else if (optionSet.has("parse")) {
            String filePath = (String) optionSet.valueOf("parse");
            logger.info("parsing " + filePath );
            proxyParser.tell(filePath, getSelf());
        }
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof List) {
            List<Proxy> parsedProxies = (List<Proxy>) o;
            proxyRepository.saveOrUpdateAll(parsedProxies);
            logger.info("success");
        }
        else {
            unhandled(o);
        }
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.start(args);

    }

    private void initActorSystem() {
        ActorSystem system = ActorSystem.create("system");

        proxyManagerRef = system.actorOf(
                Props.create(ProxyManager.class,(Creator<ProxyManager>) () ->
                        new ProxyManager(proxyRepository)));

        proxyParser = system.actorOf(Props.create(ProxyParser.class));
    }

    private OptionParser initCLIParser() {
        return new OptionParser() {
                {
                    accepts("request").withRequiredArg();
                    accepts("check");
                    accepts("parse").withRequiredArg();
                }
            };
    }
}
