package com.github.nekolr.peashooter.newznab;

import com.github.nekolr.peashooter.newznab.model.NewznabItem;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.github.nekolr.peashooter.constant.Peashooter.*;

/**
 * Newznab/Torznab XML 生成器
 */
@Component
public class NewznabXmlWriter {

    private static final Namespace NEWZNAB = Namespace.getNamespace("newznab", NEWZNAB_ATTR_NAMESPACE);

    /**
     * Sonarr 的 TorznabRssParser 只读取 torznab 命名空间下的 attr（size/tvdbid/infohash/magneturl/seeders...），
     * 因此每个属性同时以 newznab:attr 与 torznab:attr 双写输出
     */
    private static final Namespace TORZNAB = Namespace.getNamespace("torznab", TORZNAB_ATTR_NAMESPACE);

    private static final DateTimeFormatter RFC822 =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US).withZone(ZoneOffset.UTC);

    /**
     * 生成能力声明
     *
     * @param baseUrl 索引器地址
     */
    public String writeCaps(String baseUrl) {
        Element caps = new Element("caps");

        caps.addContent(new Element("server")
                .setAttribute("version", NEWZNAB_API_VERSION)
                .setAttribute("title", APPLICATION_NAME)
                .setAttribute("strapline", RSS_DESCRIPTION)
                .setAttribute("email", "")
                .setAttribute("url", baseUrl == null ? "" : baseUrl));

        caps.addContent(new Element("limits")
                .setAttribute("max", String.valueOf(NEWZNAB_MAX_LIMIT))
                .setAttribute("default", String.valueOf(NEWZNAB_DEFAULT_LIMIT)));

        // Torznab 服务应省略 retention（那是 Newznab 的 Usenet 保留期概念）
        caps.addContent(new Element("registration").setAttribute("available", "no").setAttribute("open", "no"));

        Element searching = new Element("searching");
        searching.addContent(new Element("search").setAttribute("available", "yes").setAttribute("supportedParams", "q"));
        searching.addContent(new Element("tv-search").setAttribute("available", "yes")
                .setAttribute("supportedParams", "q,tvdbid,season,ep,cat"));
        searching.addContent(new Element("movie-search").setAttribute("available", "no").setAttribute("supportedParams", "q"));
        searching.addContent(new Element("audio-search").setAttribute("available", "no").setAttribute("supportedParams", "q"));
        searching.addContent(new Element("book-search").setAttribute("available", "no").setAttribute("supportedParams", "q"));
        caps.addContent(searching);

        Element categories = new Element("categories");
        Element tv = new Element("category")
                .setAttribute("id", String.valueOf(NEWZNAB_CATEGORY_TV))
                .setAttribute("name", "TV");
        tv.addContent(new Element("subcat")
                .setAttribute("id", String.valueOf(NEWZNAB_CATEGORY_TV_WEB_DL))
                .setAttribute("name", "WEB-DL"));
        tv.addContent(new Element("subcat")
                .setAttribute("id", String.valueOf(NEWZNAB_CATEGORY_ANIME))
                .setAttribute("name", "Anime"));
        categories.addContent(tv);
        caps.addContent(categories);

        return output(new Document(caps));
    }

    /**
     * 生成搜索结果
     *
     * @param items   当前页条目
     * @param offset  偏移量
     * @param total   总条数
     * @param baseUrl 索引器地址
     */
    public String writeSearchResult(List<NewznabItem> items, int offset, int total, String baseUrl) {
        Element rss = new Element("rss").setAttribute("version", "2.0");
        rss.addNamespaceDeclaration(NEWZNAB);
        rss.addNamespaceDeclaration(TORZNAB);

        Element channel = new Element("channel");
        channel.addContent(new Element("title").setText(RSS_TITLE));
        channel.addContent(new Element("description").setText(RSS_DESCRIPTION));
        channel.addContent(new Element("link").setText(baseUrl == null ? "" : baseUrl));

        Element response = new Element("response", NEWZNAB)
                .setAttribute("offset", String.valueOf(offset))
                .setAttribute("total", String.valueOf(total));
        channel.addContent(response);

        if (items != null) {
            for (NewznabItem item : items) {
                channel.addContent(toItemElement(item));
            }
        }

        rss.addContent(channel);
        return output(new Document(rss));
    }

    /**
     * 生成错误响应
     */
    public String writeError(int code, String description) {
        Element error = new Element("error")
                .setAttribute("code", String.valueOf(code))
                .setAttribute("description", description == null ? "" : description);
        return output(new Document(error));
    }

    private Element toItemElement(NewznabItem item) {
        Element element = new Element("item");
        element.addContent(new Element("title").setText(item.title()));

        Element guid = new Element("guid").setText(item.guid());
        guid.setAttribute("isPermaLink", "false");
        element.addContent(guid);

        String link = item.enclosureUrl();
        element.addContent(new Element("link").setText(link == null ? "" : link));

        String pubDate = formatDate(item.pubDate());
        if (pubDate != null) {
            element.addContent(new Element("pubDate").setText(pubDate));
        }
        element.addContent(new Element("size").setText(String.valueOf(item.size())));
        for (Integer category : item.categories()) {
            element.addContent(new Element("category").setText(String.valueOf(category)));
        }

        Element enclosure = new Element("enclosure");
        enclosure.setAttribute("url", item.enclosureUrl() == null ? "" : item.enclosureUrl());
        enclosure.setAttribute("length", String.valueOf(item.size()));
        enclosure.setAttribute("type", item.enclosureType() == null ? "application/x-bittorrent" : item.enclosureType());
        element.addContent(enclosure);

        for (Integer category : item.categories()) {
            addAttr(element, "category", String.valueOf(category));
        }
        addAttr(element, "size", String.valueOf(item.size()));
        if (item.season() != null) {
            addAttr(element, "season", String.valueOf(item.season()));
        }
        if (item.episode() != null) {
            addAttr(element, "episode", String.valueOf(item.episode()));
        }
        if (item.tvdbId() != null) {
            addAttr(element, "tvdbid", String.valueOf(item.tvdbId()));
        }
        if (item.magnetUrl() != null) {
            addAttr(element, "magneturl", item.magnetUrl());
        }
        if (item.infoHash() != null) {
            addAttr(element, "infohash", item.infoHash());
        }
        return element;
    }

    /**
     * 同时以 newznab:attr 与 torznab:attr 输出属性
     */
    private void addAttr(Element element, String name, String value) {
        element.addContent(attr(NEWZNAB, name, value));
        element.addContent(attr(TORZNAB, name, value));
    }

    private Element attr(Namespace ns, String name, String value) {
        return new Element("attr", ns)
                .setAttribute("name", name)
                .setAttribute("value", value == null ? "" : value);
    }

    private String formatDate(Date date) {
        return date == null ? null : RFC822.format(date.toInstant());
    }

    private String output(Document document) {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat().setEncoding(CHARSET.toUpperCase(Locale.ROOT)));
        return outputter.outputString(document);
    }
}
