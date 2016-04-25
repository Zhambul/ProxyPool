package test.validation;

import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import test.entity.Ip;
import test.event.ProxyResponseEvent;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by user on 25.04.2016.
 */
public class ValidationAnonymityOnAzenvNet implements ValidationStrategy {

    private LoggingAdapter logger;
    private String myIp;

    public ValidationAnonymityOnAzenvNet(ActorSystem actorSystem) {
        logger = Logging.getLogger(actorSystem, this);
    }

    @Override
    public boolean validate(ProxyResponseEvent responseEvent) {
        if(!responseEvent.getUrl().contains("azenv.net")) {
            return true;
        }

        if(myIp == null) {
            myIp = getMyIp();
        }

        String ip = null;
        try {
            String body = EntityUtils.toString(responseEvent.getResponse().getEntity());
            String[] lines = body.split("\n");
            for (String line : lines) {
                if(line.contains("REMOTE_ADDR")) {
                    ip = line.replace("REMOTE_ADDR = ","");
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(ip == null) {
            logger.debug("no value in body");
            return true;
        }
        if(myIp.equals(ip)){
            logger.info("ip is matching");
            return false;
        }

        return true;
    }

    private String getMyIp() {
        logger.debug("getting my ip");
        URI uri;
        try {
            uri = new URIBuilder()
                    .setScheme("http")
                    .setHost("api.ipify.org")
                    .setParameter("format", "json")
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        HttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(uri);
        InputStream content;
        try {
            HttpResponse response = httpClient.execute(request);
            content = response.getEntity().getContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Ip ip = null;
        try {
            ip = new ObjectMapper().readValue(content, Ip.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ip.ip;
    }


}
