package com.github.nekolr.peashooter.rss.convertor;

import com.github.nekolr.peashooter.rss.Item;
import com.rometools.rome.feed.synd.SyndEntry;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * RSS 转换器
 */
public interface RssConvertor {

    /**
     * 将转换后的 item 列表组合成 xml
     */
    String combine(List<Item> items, @Nullable Long groupId);

    /**
     * 转换 entry
     *
     * @param entry   rss 中的 item
     * @param context 转换上下文
     */
    Item convert(SyndEntry entry, ConvertContext context);

}
