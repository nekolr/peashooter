package com.github.nekolr.peashooter.rss.convertor;

import com.github.nekolr.peashooter.api.sonarr.SonarrV3Api;
import com.github.nekolr.peashooter.api.sonarr.rsp.Series;
import com.github.nekolr.peashooter.config.SettingsManager;
import com.github.nekolr.peashooter.rss.Enclosure;
import com.github.nekolr.peashooter.rss.Item;
import com.github.nekolr.peashooter.util.*;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

import static com.github.nekolr.peashooter.constant.Peashooter.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class RssConvertorImpl implements RssConvertor {

    private final SonarrV3Api sonarrV3Api;
    private final SettingsManager settingsManager;
    private final ResolvePubDateUtil resolvePubDateUtil;

    @Override
    public String combine(List<Item> items, Long groupId) {

        String mappingUrl = settingsManager.get().getBasic().getMappingUrl();

        SyndFeed syndFeed = FeedUtils.createFeed();
        FeedUtils.setFeedType(syndFeed, RSS_2_0);
        FeedUtils.setTitle(syndFeed, RSS_TITLE);
        if (Objects.nonNull(groupId)) {
            FeedUtils.setLink(syndFeed, getGroupRssFileUrl(mappingUrl, groupId));
        } else {
            FeedUtils.setLink(syndFeed, getAutomatedGroupRssFileUrl(mappingUrl));
        }
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
        return FeedUtils.writeString(syndFeed);
    }

    @Override
    public Item convert(SyndEntry entry, ConvertContext ctx) {
        Item item = null;
        String epTitle = FeedUtils.getTitle(entry);
        List<Matcher> matchers = ctx.matchers();
        Series series = sonarrV3Api.getSeries(ctx.referenceId());
        if (Objects.isNull(series)) {
            log.warn("转换分组 {} 失败，Entry Title：{}", ctx.groupId(), epTitle);
            return null;
        }
        String mappingUrl = settingsManager.get().getBasic().getMappingUrl();
        for (Matcher matcher : matchers) {
            Pattern pattern = Pattern.compile(matcher.regexp());
            java.util.regex.Matcher m = pattern.matcher(epTitle);
            if (m.find(matcher.offset())) {
                String episodeNum = m.group(EPISODE_NUM_GROUP_NAME);
                if (Objects.nonNull(matcher.episodeOffset())) {
                    episodeNum = String.valueOf(Integer.parseInt(episodeNum) + matcher.episodeOffset());
                }

                epTitle = EpisodeTitleUtil.formatEpisodeTitle(series.title(), matcher.season(), episodeNum, ctx.quality(), ctx.language());

                String link = FeedUtils.getLink(entry);
                String guid = FeedUtils.getUri(entry);

                Date pubDate = resolvePubDateUtil.resolvePubDate(entry, link);

                List<SyndEnclosure> enclosures = FeedUtils.getEnclosures(entry);
                Integer season = matcher.season();
                SyndEnclosure first = enclosures.getFirst();
                String referenceId = ctx.referenceId();
                String url = GetTorrentLinkUtil.formatLink(mappingUrl, first.getUrl(), epTitle, episodeNum, season, referenceId);

                Enclosure enclosure = new Enclosure(url, first.getLength(), first.getType());
                item = new Item(epTitle, link, pubDate, guid, enclosure);
            }
        }
        return item;
    }

}
