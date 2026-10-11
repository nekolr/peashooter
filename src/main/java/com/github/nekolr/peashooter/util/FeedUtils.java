package com.github.nekolr.peashooter.util;

import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.SyndFeedOutput;
import org.jdom2.Element;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

public class FeedUtils {

    /**
     * 解析 feed 文本。
     * <p>
     * 直接交给 ROME 解析，不要自己用 SAXBuilder 建 Document 再塞进来：ROME 内部的
     * WireFeedInput 会禁用外部实体 / 外部 DTD 并关闭实体展开（防 XXE / SSRF / DoS），
     * 而裸的 SAXBuilder 默认允许实体展开（jdom2 默认 expandEntities=true），
     * 相当于绕过了这层防护。
     */
    public static SyndFeed getFeed(String xml) {
        try (StringReader reader = new StringReader(xml)) {
            return new SyndFeedInput().build(reader);
        } catch (Exception e) {
            throw new RuntimeException("解析 feed 失败", e);
        }
    }

    public static String getAuthor(SyndFeed feed) {
        if (Objects.nonNull(feed)) {
            return feed.getAuthor();
        }
        return null;
    }

    public static String getTitle(SyndFeed feed) {
        if (Objects.nonNull(feed)) {
            return feed.getTitle();
        }
        return null;
    }

    public static void setTitle(SyndFeed feed, String title) {
        if (Objects.nonNull(feed)) {
            feed.setTitle(title);
        }
    }

    public static String getGenerator(SyndFeed feed) {
        if (Objects.nonNull(feed)) {
            return feed.getGenerator();
        }
        return null;
    }

    public static String getLink(SyndFeed feed) {
        if (Objects.nonNull(feed)) {
            return feed.getLink();
        }
        return null;
    }

    public static void setLink(SyndFeed feed, String link) {
        if (Objects.nonNull(feed)) {
            feed.setLink(link);
        }
    }

    public static String getDescription(SyndFeed feed) {
        if (Objects.nonNull(feed)) {
            return feed.getDescription();
        }
        return null;
    }

    public static void setDescription(SyndFeed feed, String desc) {
        if (Objects.nonNull(feed)) {
            feed.setDescription(desc);
        }
    }

    public static String getWebMaster(SyndFeed feed) {
        if (Objects.nonNull(feed)) {
            return feed.getWebMaster();
        }
        return null;
    }

    public static List<SyndEntry> getEntries(SyndFeed feed) {
        if (Objects.nonNull(feed)) {
            return feed.getEntries();
        }
        return Collections.emptyList();
    }

    public static void setEntries(SyndFeed feed, List<SyndEntry> entries) {
        if (Objects.nonNull(feed)) {
            feed.setEntries(entries);
        }
    }

    public static String getAuthor(SyndEntry entry) {
        if (Objects.nonNull(entry)) {
            return entry.getAuthor();
        }
        return null;
    }

    public static String getLink(SyndEntry entry) {
        if (Objects.nonNull(entry)) {
            return entry.getLink();
        }
        return null;
    }

    public static void setLink(SyndEntry entry, String link) {
        if (Objects.nonNull(entry)) {
            entry.setLink(link);
        }
    }

    public static String getTitle(SyndEntry entry) {
        if (Objects.nonNull(entry)) {
            return entry.getTitle();
        }
        return null;
    }

    public static void setTitle(SyndEntry entry, String title) {
        if (Objects.nonNull(entry)) {
            entry.setTitle(title);
        }
    }

    public static SyndContent getTitleEx(SyndEntry entry) {
        if (Objects.nonNull(entry)) {
            return entry.getTitleEx();
        }
        return null;
    }

    public static void setTitleEx(SyndEntry entry, SyndContent title) {
        if (Objects.nonNull(entry)) {
            entry.setTitleEx(title);
        }
    }

    public static String getUri(SyndEntry entry) {
        if (Objects.nonNull(entry)) {
            return entry.getUri();
        }
        return null;
    }

    public static void setUri(SyndEntry entry, String uri) {
        if (Objects.nonNull(entry)) {
            entry.setUri(uri);
        }
    }

    public static String getDescription(SyndEntry entry) {
        if (Objects.nonNull(entry)) {
            SyndContent content = entry.getDescription();
            if (Objects.nonNull(content)) {
                return content.getValue();
            }
        }
        return null;
    }

    public static Date getPublishedDate(SyndEntry entry) {
        if (Objects.nonNull(entry)) {
            return entry.getPublishedDate();
        }
        return null;
    }

    public static void setPublishedDate(SyndEntry entry, Date date) {
        if (Objects.nonNull(entry)) {
            entry.setPublishedDate(date);
        }
    }

    public static SyndContent getDescriptionEx(SyndEntry entry) {
        if (Objects.nonNull(entry)) {
            return entry.getDescription();
        }
        return null;
    }

    public static String getContentType(SyndContent content) {
        if (Objects.nonNull(content)) {
            return content.getType();
        }
        return null;
    }

    public static String getContentValue(SyndContent content) {
        if (Objects.nonNull(content)) {
            return content.getValue();
        }
        return null;
    }

    public static List<SyndEnclosure> getEnclosures(SyndEntry entry) {
        if (Objects.nonNull(entry)) {
            return entry.getEnclosures();
        }
        return Collections.emptyList();
    }

    public static void setEnclosures(SyndEntry entry, List<SyndEnclosure> enclosures) {
        if (Objects.nonNull(entry)) {
            entry.setEnclosures(enclosures);
        }
    }

    public static String getEnclosureUrl(SyndEnclosure enclosure) {
        if (Objects.nonNull(enclosure)) {
            return enclosure.getUrl();
        }
        return null;
    }

    public static String getEnclosureType(SyndEnclosure enclosure) {
        if (Objects.nonNull(enclosure)) {
            return enclosure.getType();
        }
        return null;
    }

    public static Long getEnclosureLength(SyndEnclosure enclosure) {
        if (Objects.nonNull(enclosure)) {
            return enclosure.getLength();
        }
        return null;
    }

    public static SyndFeed createFeed() {
        return new SyndFeedImpl();
    }

    public static void setFeedType(SyndFeed feed, String type) {
        if (Objects.nonNull(feed)) {
            feed.setFeedType(type);
        }
    }

    public static SyndEntry createEntry() {
        return new SyndEntryImpl();
    }

    public static SyndEnclosure createEnclosure() {
        return new SyndEnclosureImpl();
    }

    /**
     * 读取外部标记（foreign markup）子节点的文本值，例如 &lt;torrent&gt;&lt;pubDate&gt;...&lt;/pubDate&gt;&lt;/torrent&gt;
     * <p>
     * 外部标记的模型类型由 ROME 决定（当前为 org.jdom2.Element），这里把它收敛在工具类内部，
     * 调用方只看到 String，不直接依赖 jdom2 的类型。
     *
     * @param entry       条目
     * @param elementName 外部标记的元素名，如 torrent
     * @param childName   外部标记下的子元素名，如 pubDate
     * @return 子元素的文本值，不存在时返回 null
     */
    public static String getForeignChildValue(SyndEntry entry, String elementName, String childName) {
        Element element = getForeignMarkup(entry, elementName);
        return getChildValue(element, childName);
    }

    private static Element getForeignMarkup(SyndEntry entry, String name) {
        if (Objects.nonNull(entry)) {
            List<Element> foreignMarkup = entry.getForeignMarkup();
            if (Objects.nonNull(foreignMarkup) & !foreignMarkup.isEmpty()) {
                Optional<Element> op = foreignMarkup.stream().filter(e -> name.equals(e.getName())).findFirst();
                if (op.isPresent()) {
                    return op.get();
                }
            }
        }
        return null;
    }

    private static String getChildValue(Element element, String name) {
        if (Objects.nonNull(element)) {
            List<Element> children = element.getChildren();
            if (Objects.nonNull(children) & !children.isEmpty()) {
                Optional<Element> op = children.stream().filter(e -> name.equals(e.getName())).findFirst();
                if (op.isPresent()) {
                    return op.get().getValue();
                }
            }
        }
        return null;
    }

    public static String writeString(SyndFeed syndFeed) {
        Writer writer = new StringWriter(1024);
        try {
            new SyndFeedOutput().output(syndFeed, writer, false);
            return writer.toString();
        } catch (IOException | FeedException e) {
            throw new RuntimeException(e);
        }
    }
}
