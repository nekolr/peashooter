package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.controller.request.datasource.GetDataSourceList;
import com.github.nekolr.peashooter.controller.request.datasource.TestRegexp;
import com.github.nekolr.peashooter.controller.response.datasource.ItemTitle;
import com.github.nekolr.peashooter.controller.response.datasource.MatchResult;
import com.github.nekolr.peashooter.entity.domain.DataSource;
import com.github.nekolr.peashooter.job.datasource.RssRefreshJobManager;
import com.github.nekolr.peashooter.repository.DataSourceRepository;
import com.github.nekolr.peashooter.rss.convertor.Matcher;
import com.github.nekolr.peashooter.rss.loader.RssLoader;
import com.github.nekolr.peashooter.rss.writer.RssWriter;
import com.github.nekolr.peashooter.service.IDataSourceService;
import com.github.nekolr.peashooter.util.FeedUtils;
import com.github.nekolr.peashooter.util.FillUpZeroUtil;
import com.github.nekolr.peashooter.util.Md5Util;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Format;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.nekolr.peashooter.constant.Peashooter.*;

@Slf4j
@Service
@CacheConfig(cacheNames = "dataSource")
@RequiredArgsConstructor
public class DataSourceServiceImpl implements IDataSourceService {

    private final RssWriter rssWriter;
    private final RssLoader rssLoader;
    private final RssRefreshJobManager jobManager;
    private final DataSourceRepository dataSourceRepository;

    @Override
    @Cacheable(key = "'all'")
    public List<DataSource> findAll() {
        return dataSourceRepository.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {@CacheEvict(key = "'all'"), @CacheEvict(key = "#p0")})
    public void removeById(Long id) {
        dataSourceRepository.deleteById(id);
        boolean success = jobManager.removeJob(String.valueOf(id));
        if (!success) {
            throw new RuntimeException("删除任务失败");
        }
    }

    @Override
    @Cacheable(key = "#p0")
    public DataSource getById(Long id) {
        return dataSourceRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(key = "'all'")
    public DataSource save(DataSource dataSource) {
        dataSource = dataSourceRepository.save(dataSource);
        if (Objects.nonNull(dataSource.getId())) {
            jobManager.removeJob(String.valueOf(dataSource.getId()));
        }
        jobManager.addJob(dataSource.getId(), dataSource.getRefreshSeconds());
        return dataSource;
    }

    @Override
    public Page<DataSource> findAllByPage(GetDataSourceList cmd, Pageable pageable) {
        DataSource dataSource = new DataSource();
        String dataSourceName = cmd.dataSourceName();
        if (StringUtils.hasText(dataSourceName)) {
            dataSource.setName(dataSourceName);
        }
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withNullHandler(ExampleMatcher.NullHandler.IGNORE)
                .withMatcher("name", ExampleMatcher.GenericPropertyMatcher.of(ExampleMatcher.StringMatcher.CONTAINING));
        return dataSourceRepository.findAll(Example.of(dataSource, matcher), pageable);
    }

    @Override
    @Caching(evict = {@CacheEvict(key = "'all'"), @CacheEvict(key = "#p0")})
    public boolean refreshRss(Long id) {
        boolean refreshed = false;
        DataSource dataSource = this.getById(id);
        if (Objects.nonNull(dataSource)) {
            String xml = rssLoader.load(dataSource.getSourceUrl(), dataSource.getUseProxy());
            if (Objects.nonNull(xml)) {
                String sign = Md5Util.md5(xml);
                if (!Objects.equals(sign, dataSource.getSignature())) {
                    dataSource.setSignature(sign);
                    this.save(dataSource);
                    rssWriter.write(xml, getDatasourceRssFilepath(id));
                    refreshed = true;
                }
            }
        }
        return refreshed;
    }

    @Override
    public List<ItemTitle> getItemTitleList(Long id, String title) {
        DataSource dataSource = this.getById(id);
        if (Objects.isNull(dataSource)) {
            return Collections.emptyList();
        } else {
            String xml = rssLoader.loadFromFile(getDatasourceRssFilepath(id));
            SyndFeed feed = FeedUtils.getFeed(xml);
            List<SyndEntry> entries = FeedUtils.getEntries(feed);
            List<ItemTitle> result = new ArrayList<>(entries.size());
            boolean findTitle = StringUtils.hasText(title);
            int idx = 0;
            for (SyndEntry entry : entries) {
                String seriesTitle = FeedUtils.getTitle(entry);
                if (!findTitle) {
                    result.add(new ItemTitle(++idx, seriesTitle));
                } else if (seriesTitle.contains(title)) {
                    result.add(new ItemTitle(++idx, seriesTitle));
                }
            }
            return result;
        }
    }

    @Override
    public List<MatchResult> testRegexp(TestRegexp cmd) {
        List<ItemTitle> itemTitles = new ArrayList<>(0);
        if (Objects.nonNull(cmd.dataSourceIds()) && cmd.dataSourceIds().length > 0) {
            Stream<ItemTitle> stream = Arrays.stream(cmd.dataSourceIds())
                    .flatMap(id -> this.getItemTitleList(Long.valueOf(id), null).stream());
            itemTitles = stream.collect(Collectors.toList());
        }
        int count = 0;
        Matcher matcher = cmd.matcher();
        Integer season = matcher.season();
        Pattern pattern = Pattern.compile(matcher.regexp());
        List<MatchResult> matchResultList = new ArrayList<>();
        for (ItemTitle itemTitle : itemTitles) {
            java.util.regex.Matcher m = pattern.matcher(itemTitle.title());
            if (m.find(matcher.offset())) {
                String episodeNum = m.group(EPISODE_NUM_GROUP_NAME);
                String epNum = FillUpZeroUtil.fill(episodeNum);
                String epTitle = EPISODE_TITLE_PREFIX + epNum;
                Format format = new MessageFormat(EPISODE_TITLE_TEMPLATE);
                Object[] args = new Object[]{cmd.series(), season, epNum, epTitle, cmd.language(), cmd.quality()};
                String newTitle = format.format(args);
                matchResultList.add(new MatchResult(++count, itemTitle.title(), newTitle, epNum));
            }
        }
        return matchResultList;
    }
}
