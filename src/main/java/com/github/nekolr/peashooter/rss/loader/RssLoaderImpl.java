package com.github.nekolr.peashooter.rss.loader;


import com.github.nekolr.peashooter.config.SettingsManager;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.http.ProxyInfo;
import jodd.http.net.SocketHttpConnectionProvider;
import jodd.io.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RssLoaderImpl implements RssLoader {
    private final SettingsManager settingsManager;

    @Override
    public String loadFromFile(String filepath) {
        try {
            return FileUtil.readString(filepath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String load(String url, boolean useProxy) {
        HttpRequest request = HttpRequest.get(url);
        if (useProxy) {
            this.setupProxy(request);
        }
        HttpResponse response = request.send();
        if (response.statusCode() != 200) {
            return null;
        }
        return response.bodyText();
    }

    private void setupProxy(HttpRequest request) {
        String proxyIp = settingsManager.get().getHttpProxy().getIp();
        Integer proxyPort = settingsManager.get().getHttpProxy().getPort();
        SocketHttpConnectionProvider provider = new SocketHttpConnectionProvider();
        provider.useProxy(ProxyInfo.httpProxy(proxyIp, proxyPort, null, null));
        request.withConnectionProvider(provider);
    }
}
