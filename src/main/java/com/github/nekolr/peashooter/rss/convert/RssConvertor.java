package com.github.nekolr.peashooter.rss.convert;

import com.github.nekolr.peashooter.rss.Item;
import com.rometools.rome.feed.synd.SyndEntry;

import java.util.List;

public interface RssConvertor {

    String convert(List<Item> items, Long groupId);

    Item convert(SyndEntry entry, ConvertContext context);

}
