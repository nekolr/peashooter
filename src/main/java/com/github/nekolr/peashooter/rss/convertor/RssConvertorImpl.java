package com.github.nekolr.peashooter.rss.convertor;

import com.github.nekolr.peashooter.api.sonarr.SonarrV3Api;
import com.github.nekolr.peashooter.api.sonarr.response.Series;
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
import org.springframework.util.StringUtils;

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
            // 没有分组 id，说明是自动分组
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
            FeedUtils.setEnclosures(entry, List.of(enclosure));

            entries.add(entry);
        }
        FeedUtils.setEntries(syndFeed, entries);
        return FeedUtils.writeString(syndFeed);
    }

    @Override
    public Item convert(SyndEntry entry, ConvertContext ctx) {
        Item item = null;
        String episodeTitle = FeedUtils.getTitle(entry);

        if (!StringUtils.hasText(episodeTitle)) {
            return item;
        }

        List<Matcher> matchers = ctx.matchers();
        Series series = sonarrV3Api.getSeries(ctx.referenceId());
        if (Objects.isNull(series)) {
            return item;
        }

        // 所有 matcher 都对原始标题匹配，避免第一个 matcher 修改标题后影响后续匹配
        String originalTitle = episodeTitle;

        for (Matcher matcher : matchers) {
            Pattern pattern = Pattern.compile(matcher.regexp());
            java.util.regex.Matcher m = pattern.matcher(originalTitle);
            int offset = Objects.requireNonNullElse(matcher.offset(), 0);
            if (m.find(offset)) {
                String episodeNum = m.group(EPISODE_NUM_GROUP_NAME);
                if (!StringUtils.hasText(episodeNum)) {
                    log.warn("条目 [{}] 未匹配到剧集编号分组，跳过", originalTitle);
                    continue;
                }

                // 剧集编号可能不是纯数字，解析失败时跳过该条目
                int parsedEpisodeNum;
                try {
                    parsedEpisodeNum = Integer.parseInt(episodeNum);
                } catch (NumberFormatException e) {
                    log.warn("条目 [{}] 的剧集编号 [{}] 解析失败，跳过", originalTitle, episodeNum);
                    continue;
                }

                if (Objects.nonNull(matcher.episodeOffset())) {
                    parsedEpisodeNum = parsedEpisodeNum + matcher.episodeOffset();
                    episodeNum = String.valueOf(parsedEpisodeNum);
                }

                episodeTitle = EpisodeTitleUtil.formatEpisodeTitle(series.title(), matcher.season(), episodeNum, ctx.language(), ctx.quality());

                String link = FeedUtils.getLink(entry);
                if (!StringUtils.hasText(link)) {
                    log.warn("条目 [{}] 缺少链接，跳过", originalTitle);
                    return null;
                }

                String guid = FeedUtils.getUri(entry);
                Date pubDate = resolvePubDateUtil.resolvePubDate(entry, link);

                Integer season = matcher.season();
                Enclosure enclosure = resolveEnclosure(entry, originalTitle);
                if (Objects.isNull(enclosure)) {
                    return null;
                }
                return new Item(episodeTitle, link, pubDate, guid, enclosure, series.id(), season, parsedEpisodeNum, null);
            }
        }
        return item;
    }

    /**
     * 解析条目的附件，优先取第一个 enclosure，没有时回退用条目链接构造
     */
    private Enclosure resolveEnclosure(SyndEntry entry, String originalTitle) {
        List<SyndEnclosure> enclosures = FeedUtils.getEnclosures(entry);
        if (Objects.nonNull(enclosures) && !enclosures.isEmpty()) {
            SyndEnclosure first = enclosures.getFirst();
            return new Enclosure(first.getUrl(), first.getLength(), first.getType());
        }

        // 没有 enclosure 时回退用条目链接构造，连链接也没有则放弃
        String link = FeedUtils.getLink(entry);
        if (StringUtils.hasText(link)) {
            log.warn("条目 [{}] 缺少 enclosure，回退使用条目链接", originalTitle);
            return new Enclosure(link, null, null);
        }

        log.warn("条目 [{}] 既没有 enclosure 也没有链接，跳过", originalTitle);
        return null;
    }

}
