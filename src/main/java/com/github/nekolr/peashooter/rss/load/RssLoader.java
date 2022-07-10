package com.github.nekolr.peashooter.rss.load;

public interface RssLoader {

    String load(String url, boolean useProxy);

    String loadFromFile(String filepath);
}
