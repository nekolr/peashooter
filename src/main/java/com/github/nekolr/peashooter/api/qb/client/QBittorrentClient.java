package com.github.nekolr.peashooter.api.qb.client;

import com.github.nekolr.peashooter.api.qb.QBittorrentApi;
import com.github.nekolr.peashooter.api.qb.rsp.AppVersion;
import com.github.nekolr.peashooter.api.qb.rsp.SID;
import com.github.nekolr.peashooter.config.SettingsManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class QBittorrentClient implements QBittorrentApi {

    private final SettingsManager settingsManager;
    private final RestClient defaultRestClient;
    private static final String COOKIE = "cookie";
    private static final String COOKIE_VALUE_PREFIX = "SID=";
    private static final String RENAME_TORRENT_URI_PARAM_HASH = "hash";
    private static final String RENAME_TORRENT_URI_PARAM_NAME = "name";
    private static final String LOGIN_URI_FORM_PARAM_USERNAME = "username";
    private static final String LOGIN_URI_FORM_PARAM_PASSWORD = "password";
    private final Map<String, SID> sidCache = new ConcurrentHashMap<>();

    @Override
    public Optional<SID> login() {
        if (sidCache.containsKey(COOKIE)) {
            return Optional.of(sidCache.get(COOKIE));
        } else {
            String url = settingsManager.get().getQbittorrent().getUrl() + LOGIN_URI;
            String username = settingsManager.get().getQbittorrent().getUsername();
            String password = settingsManager.get().getQbittorrent().getPassword();

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add(LOGIN_URI_FORM_PARAM_USERNAME, username);
            formData.add(LOGIN_URI_FORM_PARAM_PASSWORD, password);

            ResponseEntity<String> response = defaultRestClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .toEntity(String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                return Optional.empty();
            }

            HttpHeaders headers = response.getHeaders();
            String setCookieHeader = headers.getFirst(HttpHeaders.SET_COOKIE);
            if (setCookieHeader != null && setCookieHeader.startsWith("SID=")) {
                String sidValue = setCookieHeader.split(";")[0].substring(4);
                SID sid = new SID(sidValue);
                sidCache.put(COOKIE, sid);
                return Optional.of(sid);
            }
            return Optional.empty();
        }
    }

    @Override
    @Retryable(multiplier = 1.5)
    public Optional<AppVersion> getAppVersion() {
        try {
            String url = settingsManager.get().getQbittorrent().getUrl() + APP_VERSION_URI;
            String cookieValue = COOKIE_VALUE_PREFIX + this.login().orElseThrow().sid();

            ResponseEntity<String> response = defaultRestClient.get()
                    .uri(url)
                    .header(HttpHeaders.COOKIE, cookieValue)
                    .retrieve()
                    .toEntity(String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                return Optional.empty();
            }

            return Optional.of(new AppVersion(response.getBody()));
        } catch (HttpServerErrorException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                sidCache.clear();
            }
            throw e;
        }
    }

    @Override
    @Retryable(multiplier = 1.5)
    public boolean renameTorrent(String hash, String name) {
        try {
            String url = settingsManager.get().getQbittorrent().getUrl() + RENAME_TORRENT_URI;
            String cookieValue = COOKIE_VALUE_PREFIX + this.login().orElseThrow().sid();

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add(RENAME_TORRENT_URI_PARAM_HASH, hash);
            formData.add(RENAME_TORRENT_URI_PARAM_NAME, name);

            ResponseEntity<Void> response = defaultRestClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header(HttpHeaders.COOKIE, cookieValue)
                    .body(formData)
                    .retrieve()
                    .toBodilessEntity();

            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpServerErrorException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                sidCache.clear();
            }
            throw e;
        }
    }
}
