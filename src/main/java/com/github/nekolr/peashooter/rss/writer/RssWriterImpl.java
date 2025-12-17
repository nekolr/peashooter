package com.github.nekolr.peashooter.rss.writer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class RssWriterImpl implements RssWriter {

    @Override
    public void write(String xml, String filepath) {
        try {
            Path path = Paths.get(filepath);
            // 确保父目录存在
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, xml, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error writing RSS file: {}", filepath, e);
            throw new RuntimeException(e);
        }
    }
}
