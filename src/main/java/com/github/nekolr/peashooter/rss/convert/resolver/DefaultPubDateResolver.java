package com.github.nekolr.peashooter.rss.convert.resolver;

import com.github.nekolr.peashooter.util.FeedUtils;
import com.rometools.rome.feed.synd.SyndEntry;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DefaultPubDateResolver implements PubDateResolver {

    @Override
    public String getType() {
        return PubDateType.DEFAULT.getType();
    }

    @Override
    public Date resolver(SyndEntry entry) {
        return FeedUtils.getPublishedDate(entry);
    }
}
