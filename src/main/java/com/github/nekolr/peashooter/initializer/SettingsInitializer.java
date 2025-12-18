package com.github.nekolr.peashooter.initializer;

import com.github.nekolr.peashooter.config.Settings;
import com.github.nekolr.peashooter.config.SettingsManager;
import com.github.nekolr.peashooter.util.JacksonUtils;
import com.github.nekolr.peashooter.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.nekolr.peashooter.constant.Peashooter.*;

@Component
@RequiredArgsConstructor
public class SettingsInitializer implements InitializingBean {
    private final SettingsManager settingsManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.initSettings();
        this.initRssDir();
    }

    private void initRssDir() throws IOException {
        createDirIfNotExists(ORIGINAL_RSS_FILE_DIR);
        createDirIfNotExists(CONVERTED_RSS_FILE_DIR);
    }

    private void initSettings() throws Exception {
        createDirIfNotExists(SETTINGS_DIR);

        final String filePath = SETTINGS_DIR + SETTINGS_FILE_NAME;
        File file = new File(filePath);

        if (!file.exists()) {
            // 从 classpath 读取默认配置
            ClassPathResource resource = new ClassPathResource(SETTINGS_FILE_NAME);
            try (InputStream inputStream = resource.getInputStream()) {
                Settings settings = JacksonUtils.tryParse(() ->
                    JacksonUtils.getObjectMapper().readValue(inputStream, Settings.class));
                settings.getBasic().setApiKey(RandomUtil.generate(32));
                settingsManager.update(settings);
            }
        } else {
            // 从文件读取现有配置
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            Settings settings = JacksonUtils.tryParse(() ->
                JacksonUtils.getObjectMapper().readValue(content, Settings.class));
            settingsManager.update(settings);
        }
    }

    private void createDirIfNotExists(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }
}
