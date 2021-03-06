package test;

import akka.actor.UntypedActor;
import com.sun.istack.internal.Nullable;
import test.entity.Proxy;
import test.event.ProxyParsedEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 10 on 18.04.2016.
 */

class ProxyParser extends UntypedActor {


    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof String) {
            String csvFilePath = (String) o;
            List<Proxy> proxies = parse(csvFilePath);
            ProxyParsedEvent proxyParsedEvent = new ProxyParsedEvent(proxies);
            getSender().tell(proxyParsedEvent,getSelf());
        }
        else {
            unhandled(o);
        }
    }

    private List<Proxy> parse(String csvFilePath) {
        String line;
        List<Proxy> proxies = new ArrayList<>();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(csvFilePath));
            while ((line = bufferedReader.readLine()) != null) {
                String[] data = line.split(",");
                if(!data[0].equals("proxy server")) {
                    String ip = parseEach(data[0].split(":")[0]);
                    String port =  parseEach(data[0].split(":")[1]);
                    String country = parseEach(data[1]);
                    String city = parseEach(data[2]);
                    String scheme = parseEach(data[3]);

                    Proxy proxy = new Proxy();
                    proxy.setIp(ip);
                    if(port!= null) {
                        proxy.setPort(Integer.parseInt(port));
                    }
                    proxy.setCountry(country);
                    proxy.setCity(city);
                    proxy.setScheme(scheme);
                    proxies.add(proxy);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return proxies;
    }

    @Nullable
    private String parseEach(String input) {
        return input.equals("-")? null : input;
    }
}
