package com.github.nekolr.peashooter.config;

import com.alibaba.fastjson2.JSON;
import com.github.nekolr.peashooter.util.RandomUtil;
import jodd.io.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
        if (!FileUtil.isExistingFolder(new File(ORIGINAL_RSS_FILE_DIR))) {
            FileUtil.mkdir(ORIGINAL_RSS_FILE_DIR);
        }
        if (!FileUtil.isExistingFolder(new File(CONVERTED_RSS_FILE_DIR))) {
            FileUtil.mkdir(CONVERTED_RSS_FILE_DIR);
        }
    }

    private void initSettings() throws Exception {

        if (!FileUtil.isExistingFolder(new File(SETTINGS_DIR))) {
            FileUtil.mkdir(SETTINGS_DIR);
        }

        final String filePath = SETTINGS_DIR + SETTINGS_FILE_NAME;
        if (!FileUtil.isExistingFile(new File(filePath))) {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(SETTINGS_FILE_NAME);
            Settings settings = JSON.parseObject(inputStream, Settings.class);
            settings.getBasic().setApiKey(RandomUtil.generate(32));
            settingsManager.update(settings);
        } else {
            settingsManager.update(JSON.parseObject(FileUtil.readString(filePath), Settings.class));
        }
    }
}
