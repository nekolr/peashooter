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
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Objects;

/**
 * 代理 RestClient 的按需提供者：
 * 每次获取时比对当前代理配置（ip:port），变更时关闭旧的连接管理器并重建 RestClient，
 * 使代理配置修改后无需重启应用即可生效。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProxyRestClientProvider {

    private final SettingsManager settingsManager;

    private volatile RestClient current;

    private volatile String lastProxyAddress = "";

    /**
     * 获取当前生效的代理 RestClient，代理配置变更时自动重建
     */
    public RestClient get() {
        String address = this.currentProxyAddress();
        if (current == null || !Objects.equals(address, lastProxyAddress)) {
            synchronized (this) {
                address = this.currentProxyAddress();
                if (current == null || !Objects.equals(address, lastProxyAddress)) {
                    RestClient old = current;
                    current = build();
                    lastProxyAddress = address;
                    if (old != null) {
                        log.info("代理配置变更，已重建代理 RestClient，当前代理: {}", address);
                    }
                }
            }
        }
        return current;
    }

    private String currentProxyAddress() {
        Settings.HttpProxy proxy = settingsManager.get().getHttpProxy();
        if (proxy == null || proxy.getIp() == null || proxy.getPort() == null || proxy.getPort() <= 0) {
            return "";
        }
        return proxy.getIp() + ":" + proxy.getPort();
    }

    private RestClient build() {
        RestClient.Builder builder = RestClient.builder();
        builder.requestFactory(createRequestFactory());
        return builder.build();
    }

    private ClientHttpRequestFactory createRequestFactory() {
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
        String proxyIp = settingsManager.get().getHttpProxy().getIp();
        Integer proxyPort = settingsManager.get().getHttpProxy().getPort();
        if (proxyIp != null && proxyPort != null && proxyPort > 0) {
            HttpHost proxy = new HttpHost("http", proxyIp, proxyPort);
            httpClientBuilder.setProxy(proxy);
            log.info("Created RestClient with HTTP proxy: {}:{}", proxyIp, proxyPort);
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
