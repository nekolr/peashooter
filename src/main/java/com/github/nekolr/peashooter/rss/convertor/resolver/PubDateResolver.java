package com.github.nekolr.peashooter.rss.convertor.resolver;

import com.rometools.rome.feed.synd.SyndEntry;

import java.util.Date;

public interface PubDateResolver {

    String getType();

    Date resolver(SyndEntry entry);
}
