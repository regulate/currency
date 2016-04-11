package org.baddev.currency.fetcher;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.ConnectionType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

/**
 * Created by IPotapchuk on 3/15/2016.
 */
@Configuration
@ComponentScan("org.baddev.currency.fetcher")
@EnableAspectJAutoProxy
@PropertySources({
        @PropertySource("classpath:api_sources.properties"),
        @PropertySource("classpath:proxy.properties")
})
public class FetcherConfig {

    private static final Logger log = LoggerFactory.getLogger(FetcherConfig.class);

    @Value("${source_nbu}") String sourceBaseURI;
    @Value("${enabled}") String proxyEnabled;
    @Value("${server}") String proxy;
    @Value("${port}") String port;

    @Bean(name = "NBUClient")
    public WebClient client() {
        WebClient client = WebClient.create(sourceBaseURI);
        HTTPConduit conduit = (HTTPConduit) WebClient.getConfig(client).getConduit();
        HTTPClientPolicy policy = conduit.getClient();
        policy.setReceiveTimeout(32000);
        policy.setAllowChunking(false);
        policy.setConnectionTimeout(36000);
        if(proxyEnabled.equals("true")){
            log.info("Using proxy mode");
            policy.setProxyServer(proxy);
            policy.setProxyServerPort(Integer.parseInt(port));
        }
        return client;
    }

    @Bean(name = "WebClientAspect")
    public WebClientAspect aspect(){
        return new WebClientAspect(client());
    }

}