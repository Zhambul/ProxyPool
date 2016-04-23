package test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import org.apache.log4j.Logger;
import test.database.ProxyRepository;
import test.database.ProxyRepositoryImpl;
import test.entity.Proxy;
import test.event.ExecuteRequestEvent;
import test.event.ProxyParseEvent;

import java.util.List;


public class Main {
    private static ProxyRepository proxyRepository = new ProxyRepositoryImpl();
    private static ActorRef proxyManagerRef;
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
        initActorSystem();
        OptionParser parser = initCLIParser();
        OptionSet optionSet = parser.parse(args);

        if (optionSet.has("request")) {
            String url = (String) optionSet.valueOf("request");
            logger.info("request to " + url);
            ExecuteRequestEvent executeRequestEvent = new ExecuteRequestEvent(url);
            proxyManagerRef.tell(executeRequestEvent, ActorRef.noSender());
        }
        else if (optionSet.has("check")) {
            logger.info("checking");
            proxyManagerRef.tell("startMonitoring",ActorRef.noSender());
        }
        else if (optionSet.has("parse")) {
            String filePath = (String) optionSet.valueOf("parse");
            logger.info("parsing " + filePath );
            ProxyParseEvent proxyParseEvent = new ProxyParseEvent(filePath);
            proxyManagerRef.tell(proxyParseEvent,ActorRef.noSender());
        }
    }

    private static void initActorSystem() {
        ActorSystem system = ActorSystem.create("system");

        proxyManagerRef = system.actorOf(
                Props.create(ProxyManager.class,(Creator<ProxyManager>) () ->
                        new ProxyManager(proxyRepository)));
    }

    private static OptionParser initCLIParser() {
        return new OptionParser() {
                {
                    accepts("request").withRequiredArg();
                    accepts("check");
                    accepts("parse").withRequiredArg();
                }
            };
    }
}
