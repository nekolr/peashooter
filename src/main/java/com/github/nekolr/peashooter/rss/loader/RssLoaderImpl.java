package com.github.nekolr.peashooter.rss.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor
public class RssLoaderImpl implements RssLoader {

    private final RestClient defaultRestClient;
    private final RestClient proxyRestClient;

    @Override
    public String loadFromFile(String filepath) {
        try {
            return Files.readString(Paths.get(filepath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error reading file: {}", filepath, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String load(String url, boolean useProxy) {
        RestClient restClient = useProxy ? proxyRestClient : defaultRestClient;
        try {
            ResponseEntity<String> response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .toEntity(String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                return null;
            }

            return response.getBody();
        } catch (Exception e) {
            log.error("Error loading file: {}, {}", url, e.getMessage());
            return null;
        }

    }
}
