package com.github.nekolr.peashooter.util;

import com.github.nekolr.peashooter.rss.convertor.resolver.PubDateResolver;
import com.github.nekolr.peashooter.rss.convertor.resolver.PubDateType;
import com.rometools.rome.feed.synd.SyndEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.nekolr.peashooter.constant.Peashooter.MI_KAN_URL;

@Component
public class ResolvePubDateUtil {

    private Map<String, PubDateResolver> resolverMap;

    @Autowired
    private void setResolverMap(List<PubDateResolver> resolvers) {
        resolverMap = resolvers.stream().collect(Collectors.toMap(PubDateResolver::getType, r -> r));
    }

    /**
     * 根据链接解析发布时间
     */
    public Date resolvePubDate(SyndEntry entry, String link) {
        if (link.contains(MI_KAN_URL)) {
            return resolverMap.get(PubDateType.MI_KAN.getType()).resolver(entry);
        } else {
            return resolverMap.get(PubDateType.DEFAULT.getType()).resolver(entry);
        }
    }

}
