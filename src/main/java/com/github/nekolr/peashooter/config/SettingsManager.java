package com.github.nekolr.peashooter.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static com.github.nekolr.peashooter.constant.Peashooter.SETTINGS_DIR;
import static com.github.nekolr.peashooter.constant.Peashooter.SETTINGS_FILE_NAME;

@Component
public class SettingsManager {
    private Settings settings;

    public Settings get() {
        return settings;
    }

    public synchronized void update(Settings settings) {
        this.settings = settings;
        this.dumpFile();
    }

    private void dumpFile() {
        try {
            String prettyJson = JSON.toJSONString(settings, JSONWriter.Feature.PrettyFormat);
            Path filepath = Paths.get(SETTINGS_DIR + SETTINGS_FILE_NAME);
            Files.writeString(filepath, prettyJson, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean validApiKey(String apiKey) {
        if (StringUtils.hasText(apiKey)) {
            return apiKey.equals(settings.getBasic().getApiKey());
        }
        return false;
    }
}
