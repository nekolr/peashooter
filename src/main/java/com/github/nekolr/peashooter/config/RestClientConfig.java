package com.github.nekolr.peashooter.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;

@Slf4j
@Component
@DependsOn("settingsInitializer")
@RequiredArgsConstructor
public class RestClientConfig {

    private final SettingsManager settingsManager;

    @Bean("defaultRestClient")
    public RestClient defaultRestClient() {
        return this.createRestClient(false);
    }

    @Bean("proxyRestClient")
    public RestClient proxyRestClient() {
        return this.createRestClient(true);
    }

    private RestClient createRestClient(boolean useProxy) {
        RestClient.Builder builder = RestClient.builder();
        ClientHttpRequestFactory factory = createRequestFactory(useProxy);

        builder.requestFactory(factory);
        return builder.build();
    }

    private ClientHttpRequestFactory createRequestFactory(boolean useProxy) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(Duration.ofSeconds(15));  // 15 秒连接超时
        factory.setReadTimeout(Duration.ofSeconds(30));     // 30 秒读取超时

        if (useProxy) {
            String proxyIp = settingsManager.get().getHttpProxy().getIp();
            Integer proxyPort = settingsManager.get().getHttpProxy().getPort();

            if (proxyIp != null && proxyPort != null && proxyPort > 0) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIp, proxyPort));
                factory.setProxy(proxy);
                log.info("Created RestClient with HTTP proxy: {}:{}", proxyIp, proxyPort);
            }
        }

        return factory;
    }
}