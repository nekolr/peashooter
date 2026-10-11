package com.github.nekolr.peashooter.rss.convertor.resolver;

import com.github.nekolr.peashooter.util.DateUtils;
import com.github.nekolr.peashooter.util.FeedUtils;
import com.rometools.rome.feed.synd.SyndEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

@Slf4j
@Component
public class MiKanPubDateResolver implements PubDateResolver {

    @Override
    public String getType() {
        return PubDateType.MI_KAN.getType();
    }

    @Override
    public Date resolver(SyndEntry entry) {
        String pubDateStr = FeedUtils.getForeignChildValue(entry, "torrent", "pubDate");
        if (StringUtils.hasText(pubDateStr)) {
            try {
                return DateUtils.parse(pubDateStr, DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e) {
                log.warn("解析 MiKan pubDate 失败: {}，回退使用条目发布时间", pubDateStr);
            }
        }
        return FeedUtils.getPublishedDate(entry);
    }
}
