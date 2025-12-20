package com.github.nekolr.peashooter.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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

        final Timeout readTimeout = Timeout.ofSeconds(30);
        final Timeout connectTimeout = Timeout.ofSeconds(15);

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        // 配置连接参数
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(connectTimeout) // 15 秒连接超时
                .build();
        connectionManager.setDefaultConnectionConfig(connectionConfig);

        // 构建 HttpClient
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
                .setConnectionManager(connectionManager);

        // 设置代理
        if (useProxy) {
            String proxyIp = settingsManager.get().getHttpProxy().getIp();
            Integer proxyPort = settingsManager.get().getHttpProxy().getPort();

            if (proxyIp != null && proxyPort != null && proxyPort > 0) {
                HttpHost proxy = new HttpHost("http", proxyIp, proxyPort);
                httpClientBuilder.setProxy(proxy);
                log.info("Created RestClient with HTTP proxy: {}:{}", proxyIp, proxyPort);
            }
        }

        // 配置请求超时
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(connectTimeout)
                .setResponseTimeout(readTimeout) // 响应超时
                .build();
        httpClientBuilder.setDefaultRequestConfig(requestConfig);

        HttpClient httpClient = httpClientBuilder.build();

        // 创建并配置 HttpComponentsClientHttpRequestFactory
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(readTimeout.toDuration()); // 30 秒读取超时
        factory.setConnectionRequestTimeout(connectTimeout.toDuration()); // 15 秒连接超时

        return factory;
    }
}