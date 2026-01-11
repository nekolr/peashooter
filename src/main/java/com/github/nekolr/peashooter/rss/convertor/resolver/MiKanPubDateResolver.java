package com.github.nekolr.peashooter.rss.convertor.resolver;

import com.github.nekolr.peashooter.util.DateUtils;
import com.github.nekolr.peashooter.util.FeedUtils;
import com.rometools.rome.feed.synd.SyndEntry;
import org.jdom2.Element;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component
public class MiKanPubDateResolver implements PubDateResolver {

    @Override
    public String getType() {
        return PubDateType.MI_KAN.getType();
    }

    @Override
    public Date resolver(SyndEntry entry) {
        Element element = FeedUtils.getForeignMarkup(entry, "torrent");
        String pubDateStr = FeedUtils.getChildValue(element, "pubDate");
        return DateUtils.parse(pubDateStr, DateTimeFormatter.ISO_DATE_TIME);
    }
}
