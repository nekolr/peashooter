package com.github.nekolr.peashooter.rss.convert;

import com.github.nekolr.peashooter.api.sonarr.SonarrApi;
import com.github.nekolr.peashooter.api.sonarr.rsp.Series;
import com.github.nekolr.peashooter.config.SettingsManager;
import com.github.nekolr.peashooter.constant.Peashooter;
import com.github.nekolr.peashooter.rss.Enclosure;
import com.github.nekolr.peashooter.rss.Item;
import com.github.nekolr.peashooter.rss.convert.resolver.PubDateResolver;
import com.github.nekolr.peashooter.rss.convert.resolver.PubDateType;
import com.github.nekolr.peashooter.util.FeedUtils;
import com.github.nekolr.peashooter.util.FillUpZeroUtil;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.nekolr.peashooter.constant.Peashooter.*;

@Component
@RequiredArgsConstructor
public class RssConvertorImpl implements RssConvertor {

    private static final String TORRENTS_URI = "/api/torrents";
    private static final String TORRENT_URI_PARAM_SERIES = "series";
    private static final String TORRENT_URI_PARAM_URL = "url";
    private static final String TORRENT_URI_PARAM_TITLE = "title";
    private static final String TORRENT_URI_PARAM_EPISODE = "episode";
    private static final String TORRENT_URI_PARAM_SEASON = "season";
    private static final String RSS_2_0 = "rss_2.0";
    private final SonarrApi sonarrApi;
    private final SettingsManager settingsManager;
    private Map<String, PubDateResolver> resolverMap;

    @Override
    public String convert(List<Item> items, Long groupId) {

        String mappingUrl = settingsManager.get().getBasic().getMappingUrl();

        SyndFeed syndFeed = FeedUtils.createFeed();
        FeedUtils.setFeedType(syndFeed, RSS_2_0);
        FeedUtils.setTitle(syndFeed, RSS_TITLE);
        FeedUtils.setLink(syndFeed, getGroupLink(mappingUrl, groupId));
        FeedUtils.setDescription(syndFeed, RSS_DESCRIPTION);

        List<SyndEntry> entries = new ArrayList<>(items.size());
        for (Item item : items) {
            SyndEntry entry = FeedUtils.createEntry();
            FeedUtils.setTitle(entry, item.title());
            FeedUtils.setUri(entry, item.guid());
            FeedUtils.setLink(entry, item.link());
            FeedUtils.setPublishedDate(entry, item.pubDate());

            SyndEnclosure enclosure = FeedUtils.createEnclosure();
            enclosure.setUrl(item.enclosure().url());
            enclosure.setType(item.enclosure().type());
            enclosure.setLength(item.enclosure().length());
            FeedUtils.setEnclosures(entry, Arrays.asList(enclosure));

            entries.add(entry);
        }
        FeedUtils.setEntries(syndFeed, entries);
        return this.writeString(syndFeed);
    }

    @Override
    public Item convert(SyndEntry entry, ConvertContext ctx) {
        Item item = null;
        String epTitle = FeedUtils.getTitle(entry);
        List<Matcher> matchers = ctx.matchers();
        Series series = sonarrApi.getSeries(ctx.referenceId());
        String mappingUrl = settingsManager.get().getBasic().getMappingUrl();
        for (Matcher matcher : matchers) {
            Pattern pattern = Pattern.compile(matcher.regexp());
            java.util.regex.Matcher m = pattern.matcher(epTitle);
            if (m.find(matcher.offset())) {
                String episodeNum = m.group(EPISODE_NUM_GROUP_NAME);
                epTitle = this.epTitle(series.title(), matcher.season(), episodeNum, ctx.quality(), ctx.language());

                String link = FeedUtils.getLink(entry);
                String guid = FeedUtils.getUri(entry);

                Date pubDate = this.resolvePubDate(entry, link);

                List<SyndEnclosure> enclosures = FeedUtils.getEnclosures(entry);
                SyndEnclosure first = enclosures.get(0);
                String season = matcher.season();
                String referenceId = ctx.referenceId();
                String url = this.getTorrentLink(mappingUrl, first.getUrl(), epTitle, episodeNum, season, referenceId);

                Enclosure enclosure = new Enclosure(url, first.getLength(), first.getType());
                item = new Item(epTitle, link, pubDate, guid, enclosure);
            }
        }
        return item;
    }

    private Date resolvePubDate(SyndEntry entry, String link) {
        if (link.indexOf(MI_KAN_URL) != -1) {
            return resolverMap.get(PubDateType.MI_KAN.getType()).resolver(entry);
        } else {
            return resolverMap.get(PubDateType.DEFAULT.getType()).resolver(entry);
        }
    }

    @Autowired
    public void setResolverMap(List<PubDateResolver> resolvers) {
        resolverMap = resolvers.stream().collect(Collectors.toMap(PubDateResolver::getType, r -> r));
    }

    private String writeString(SyndFeed syndFeed) {
        Writer writer = new StringWriter(1024);
        try {
            new SyndFeedOutput().output(syndFeed, writer, false);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (FeedException e) {
            throw new RuntimeException(e);
        }
    }

    private String epTitle(String seriesTitle, String season, String episodeNum, String quality, String language) {
        String epNum = FillUpZeroUtil.fill(episodeNum);
        String seasonNum = FillUpZeroUtil.fill(season);
        String epTitle = Peashooter.EPISODE_TITLE_PREFIX + epNum;
        return MessageFormat.format(EPISODE_TITLE_TEMPLATE, seriesTitle, seasonNum, epNum, epTitle, language, quality);
    }

    private String getTorrentLink(String mappingUrl, String url, String title,
                                  String episode, String season, String series) {
        try {
            title = URLEncoder.encode(title, CHARSET);
            return new StringBuilder(mappingUrl)
                    .append(TORRENTS_URI).append(QUESTION)
                    .append(TORRENT_URI_PARAM_URL).append(EQUALS).append(url)
                    .append(AND)
                    .append(TORRENT_URI_PARAM_TITLE).append(EQUALS).append(title)
                    .append(AND)
                    .append(TORRENT_URI_PARAM_EPISODE).append(EQUALS).append(episode)
                    .append(AND)
                    .append(TORRENT_URI_PARAM_SEASON).append(EQUALS).append(season)
                    .append(AND)
                    .append(TORRENT_URI_PARAM_SERIES).append(EQUALS).append(series)
                    .toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
