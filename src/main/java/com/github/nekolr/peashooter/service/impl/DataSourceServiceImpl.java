package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.controller.cmd.datasource.GetDataSourceListCmd;
import com.github.nekolr.peashooter.controller.cmd.datasource.TestRegexpCmd;
import com.github.nekolr.peashooter.controller.vo.datasource.ItemTitleVo;
import com.github.nekolr.peashooter.controller.vo.datasource.MatchResultVo;
import com.github.nekolr.peashooter.entity.domain.DataSource;
import com.github.nekolr.peashooter.job.datasource.DataSourceRssRefreshJobManager;
import com.github.nekolr.peashooter.repository.DataSourceRepository;
import com.github.nekolr.peashooter.rss.convertor.Matcher;
import com.github.nekolr.peashooter.rss.loader.RssLoader;
import com.github.nekolr.peashooter.rss.writer.RssWriter;
import com.github.nekolr.peashooter.service.IDataSourceService;
import com.github.nekolr.peashooter.util.EpisodeTitleUtil;
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

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.nekolr.peashooter.constant.Peashooter.*;

@Slf4j
@Service
@CacheConfig(cacheNames = "dataSource")
@RequiredArgsConstructor
public class DataSourceServiceImpl implements IDataSourceService {

    private final RssWriter rssWriter;
    private final RssLoader rssLoader;
    private final DataSourceRssRefreshJobManager jobManager;
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
    public Page<DataSource> findAllByPage(GetDataSourceListCmd cmd, Pageable pageable) {
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
        DataSource dataSource = dataSourceRepository.findById(id).orElse(null);
        if (Objects.nonNull(dataSource)) {
            String xml = rssLoader.load(dataSource.getSourceUrl(), dataSource.getUseProxy());
            if (Objects.nonNull(xml)) {
                String sign = Md5Util.md5(xml);
                if (!Objects.equals(sign, dataSource.getSignature())) {
                    dataSource.setSignature(sign);
                    this.dataSourceRepository.save(dataSource);
                    rssWriter.write(xml, getDatasourceRssFilepath(id));
                    refreshed = true;
                }
            }
        }
        return refreshed;
    }

    @Override
    public List<ItemTitleVo> getItemTitleList(Long id, String title) {
        DataSource dataSource = dataSourceRepository.findById(id).orElse(null);
        if (Objects.isNull(dataSource)) {
            return Collections.emptyList();
        } else {
            String xml = rssLoader.loadFromFile(getDatasourceRssFilepath(id));
            SyndFeed feed = FeedUtils.getFeed(xml);
            List<SyndEntry> entries = FeedUtils.getEntries(feed);
            List<ItemTitleVo> result = new ArrayList<>(entries.size());
            boolean findTitle = StringUtils.hasText(title); // 是否要查找标题
            int idx = 0;
            for (SyndEntry entry : entries) {
                String seriesTitle = FeedUtils.getTitle(entry);
                if (Objects.nonNull(seriesTitle)) {
                    if (!findTitle) {
                        result.add(new ItemTitleVo(++idx, seriesTitle));
                    } else {
                        if (seriesTitle.contains(title)) {
                            result.add(new ItemTitleVo(++idx, seriesTitle));
                        }
                    }
                }
            }
            return result;
        }
    }


    @Override
    public List<MatchResultVo> testRegexp(TestRegexpCmd cmd) {
        List<ItemTitleVo> itemTitleVos = new ArrayList<>(0);
        if (Objects.nonNull(cmd.dataSourceIds()) && cmd.dataSourceIds().length > 0) {
            Stream<ItemTitleVo> stream = Arrays.stream(cmd.dataSourceIds())
                    .flatMap(id -> getItemTitleList(Long.valueOf(id), null).stream());
            itemTitleVos = stream.toList();
        }
        int count = 0;
        Matcher matcher = cmd.matcher();
        Integer season = matcher.season();
        Integer episodeOffset = matcher.episodeOffset();
        Pattern pattern = Pattern.compile(matcher.regexp());
        List<MatchResultVo> matchResultVoList = new ArrayList<>();
        for (ItemTitleVo itemTitleVo : itemTitleVos) {
            java.util.regex.Matcher m = pattern.matcher(itemTitleVo.title());
            if (m.find(matcher.offset())) {
                String episodeNumStr = m.group(EPISODE_NUM_GROUP_NAME);
                // 校正后的集数
                int adjustedEpisodeNum = Integer.parseInt(episodeNumStr) + (episodeOffset != null ? episodeOffset : 0);
                String episodeNum = FillUpZeroUtil.fill(String.valueOf(adjustedEpisodeNum));
                // 格式化后的集标题
                String newTitle = EpisodeTitleUtil.formatEpisodeTitle(cmd.seriesTitle(), season, episodeNum, cmd.language(), cmd.quality());
                matchResultVoList.add(new MatchResultVo(++count, itemTitleVo.title(), newTitle, episodeNum));
            }
        }
        return matchResultVoList;
    }
}
