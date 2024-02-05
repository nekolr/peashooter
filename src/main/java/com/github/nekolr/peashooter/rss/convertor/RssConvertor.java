package com.github.nekolr.peashooter.rss.convertor;

import com.github.nekolr.peashooter.rss.Item;
import com.rometools.rome.feed.synd.SyndEntry;

import java.util.List;

public interface RssConvertor {

    /**
     * 将转换后的 Item 列表组合成 xml
     */
    String combine(List<Item> items, Long groupId);

    Item convert(SyndEntry entry, ConvertContext context);

}
