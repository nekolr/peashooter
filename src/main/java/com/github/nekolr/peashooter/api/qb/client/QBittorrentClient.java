package com.github.nekolr.peashooter.api.qb.client;

import com.github.nekolr.peashooter.api.qb.QBittorrentApi;
import com.github.nekolr.peashooter.api.qb.rsp.AppVersion;
import com.github.nekolr.peashooter.api.qb.rsp.SID;
import com.github.nekolr.peashooter.config.SettingsManager;
import jodd.http.Cookie;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QBittorrentClient implements QBittorrentApi {

    private final SettingsManager settingsManager;
    private static final String COOKIE = "cookie";
    private static final String COOKIE_VALUE_PREFIX = "SID=";
    private static final String RENAME_TORRENT_URI_PARAM_HASH = "hash";
    private static final String RENAME_TORRENT_URI_PARAM_NAME = "name";
    private static final String LOGIN_URI_FORM_PARAM_USERNAME = "username";
    private static final String LOGIN_URI_FORM_PARAM_PASSWORD = "password";

    @Override
    public SID login() {
        String url = settingsManager.get().getQbittorrent().getUrl() + LOGIN_URI;
        String username = settingsManager.get().getQbittorrent().getUsername();
        String password = settingsManager.get().getQbittorrent().getPassword();
        HttpRequest request = HttpRequest.post(url);
        request.form(LOGIN_URI_FORM_PARAM_USERNAME, username);
        request.form(LOGIN_URI_FORM_PARAM_PASSWORD, password);
        HttpResponse response = request.send();
        if (response.statusCode() != 200)
            return null;
        Cookie[] cookies = response.cookies();
        return new SID(cookies[0].getValue());
    }

    @Override
    public AppVersion getAppVersion() {
        String url = settingsManager.get().getQbittorrent().getUrl() + APP_VERSION_URI;
        HttpRequest request = HttpRequest.get(url);
        try {
            request.header(COOKIE, COOKIE_VALUE_PREFIX + this.login().sid());
            HttpResponse response = request.send();
            if (response.statusCode() != 200)
                return null;
            return new AppVersion(response.bodyText());
        } catch (Exception e) {
            log.error("get app version error: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean renameTorrent(String hash, String name) {
        String url = settingsManager.get().getQbittorrent().getUrl() + RENAME_TORRENT_URI;
        HttpRequest request = HttpRequest.post(url);
        request.header(COOKIE, COOKIE_VALUE_PREFIX + this.login().sid());
        request.form(RENAME_TORRENT_URI_PARAM_HASH, hash);
        request.form(RENAME_TORRENT_URI_PARAM_NAME, name);
        HttpResponse response = request.send();
        return response.statusCode() == 200;
    }
}
