package com.github.nekolr.peashooter.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import jodd.io.FileUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

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
        String prettyJson = JSON.toJSONString(settings, JSONWriter.Feature.PrettyFormat);
        try {
            String filepath = SETTINGS_DIR + SETTINGS_FILE_NAME;
            FileUtil.writeString(filepath, prettyJson);
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
