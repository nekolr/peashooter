package com.github.nekolr.peashooter.config;

import com.github.nekolr.peashooter.util.JacksonUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static com.github.nekolr.peashooter.constant.Peashooter.SETTINGS_DIR;
import static com.github.nekolr.peashooter.constant.Peashooter.SETTINGS_FILE_NAME;

@Component
public class SettingsManager {
    /**
     * volatile 保证读线程能及时看到 update() 的整体替换；
     * get() 返回的是当前快照，调用方不应原地修改子对象（应整体替换后 update）
     */
    private volatile Settings settings;

    /**
     * 获取全部配置
     */
    public Settings get() {
        return settings;
    }

    /**
     * 更新配置
     */
    public synchronized void update(Settings settings) {
        this.settings = settings;
        this.dumpFile();
    }

    /**
     * 写配置文件
     */
    private void dumpFile() {
        try {
            String prettyJson = JacksonUtils.tryParse(() ->
                    JacksonUtils.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(settings));
            Path filepath = Paths.get(SETTINGS_DIR + SETTINGS_FILE_NAME);
            Files.writeString(filepath, prettyJson, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 验证 apiKey
     */
    public boolean validApiKey(String apiKey) {
        if (StringUtils.hasText(apiKey)) {
            // 常量时间比较，避免时序攻击逐位猜测 api key
            String stored = settings.getBasic().getApiKey();
            return stored != null && MessageDigest.isEqual(
                    apiKey.getBytes(StandardCharsets.UTF_8),
                    stored.getBytes(StandardCharsets.UTF_8));
        }
        return false;
    }
}
