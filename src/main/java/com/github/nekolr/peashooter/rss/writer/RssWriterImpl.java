package com.github.nekolr.peashooter.rss.writer;

import jodd.io.FileUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RssWriterImpl implements RssWriter {

    @Override
    public void write(String xml, String filepath) {
        try {
            FileUtil.writeString(filepath, xml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
