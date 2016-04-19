package test;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.log4j.PropertyConfigurator;
import test.database.ProxyRepository;
import test.database.ProxyRepositoryImpl;
import test.entity.Proxy;

import java.util.List;


public class Main {

    public static void main(String[] args) throws Exception {
        String log4jConfPath = "log4j.properties";
        PropertyConfigurator.configure(log4jConfPath);
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

        if(optionSet.has("request")) {
            String url = (String) optionSet.valueOf("request");
            test.util.Logger.i("request to "  + url);
            proxyManager.executeProxyRequest(url);
        }

        if(optionSet.has("check")) {
            proxyManager.startMonitoring();
        }
        else if (optionSet.has("parse")) {
            String filePath = (String) optionSet.valueOf("parse");
            proxyParser = new ProxyParser(filePath);
            List<Proxy> parsedProxies = proxyParser.parse();
            proxyRepository.saveOrUpdateAll(parsedProxies);
        }
    }
}
