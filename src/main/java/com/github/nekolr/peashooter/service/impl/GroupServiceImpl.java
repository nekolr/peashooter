package com.github.nekolr.peashooter.service.impl;

import com.alibaba.fastjson2.JSON;
import com.github.nekolr.peashooter.config.SettingsManager;
import com.github.nekolr.peashooter.controller.request.group.SaveGroup;
import com.github.nekolr.peashooter.controller.request.group.GetGroupList;
import com.github.nekolr.peashooter.entity.domain.Group;
import com.github.nekolr.peashooter.entity.domain.GroupDataSource;
import com.github.nekolr.peashooter.entity.mapper.GroupMapper;
import com.github.nekolr.peashooter.repository.GroupRepository;
import com.github.nekolr.peashooter.rss.Item;
import com.github.nekolr.peashooter.rss.convertor.ConvertContext;
import com.github.nekolr.peashooter.rss.convertor.Matcher;
import com.github.nekolr.peashooter.rss.convertor.RssConvertor;
import com.github.nekolr.peashooter.rss.loader.RssLoader;
import com.github.nekolr.peashooter.rss.writer.RssWriter;
import com.github.nekolr.peashooter.service.IGroupDataSourceService;
import com.github.nekolr.peashooter.service.IGroupService;
import com.github.nekolr.peashooter.util.FeedUtils;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.github.nekolr.peashooter.constant.Peashooter.*;
import static com.github.nekolr.peashooter.constant.Peashooter.RSS_DESCRIPTION;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements IGroupService {

    private final RssWriter rssWriter;
    private final RssLoader rssLoader;
    private final GroupMapper groupMapper;
    private final RssConvertor rssConvertor;
    private final GroupRepository groupRepository;
    private final SettingsManager settingsManager;
    private final IGroupDataSourceService gdService;

    @Override
    public List<Group> findAll() {
        return groupRepository.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeById(Long id) {
        groupRepository.deleteById(id);
        List<GroupDataSource> groupDataSources = gdService.getByGroupId(id);
        groupDataSources.forEach(gd -> gdService.removeById(gd.getId()));
    }

    @Override
    public Group getById(Long id) {
        Group group = groupRepository.findById(id).orElse(null);
        if (Objects.isNull(group)) {
            throw new RuntimeException("group not found, id: " + id);
        }
        List<GroupDataSource> gdList = gdService.getByGroupId(group.getId());
        List<Long> dsList = gdList.stream().map(GroupDataSource::getDatasourceId).toList();
        group.setDataSourceIds(dsList.toArray(new Long[dsList.size()]));
        group.setMatchers(JSON.parseArray(group.getMatchersJson(), Matcher.class));
        return group;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Group save(Group group) {
        group.setUpdateTime(new Date());
        return groupRepository.save(group);
    }

    @Override
    public Page<Group> findAllByPage(GetGroupList cmd, Pageable pageable) {
        Group group = new Group();
        String groupName = cmd.groupName();
        if (StringUtils.hasText(groupName)) {
            group.setName(groupName);
        }
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withNullHandler(ExampleMatcher.NullHandler.IGNORE)
                .withMatcher("name", ExampleMatcher.GenericPropertyMatcher.of(ExampleMatcher.StringMatcher.CONTAINING));
        Page<Group> page = groupRepository.findAll(Example.of(group, matcher), pageable);
        List<Group> list = page.getContent();
        list.stream().forEach(e -> e.setRssLink(getGroupRssFileUrl(settingsManager.get().getBasic().getMappingUrl(), e.getId())));

        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGroup(SaveGroup saveGroup) {
        Group group = groupMapper.toDomain(saveGroup);

        if (!CollectionUtils.isEmpty(saveGroup.matchers())) {
            group.setMatchersJson(JSON.toJSONString(saveGroup.matchers()));
        }
        this.save(group);

        if (Objects.nonNull(group.getId())) {
            List<GroupDataSource> needDelList = gdService.getByGroupId(group.getId());
            gdService.deleteList(needDelList);
        }

        Long[] dataSourceIds = saveGroup.dataSourceIds();
        if (Objects.nonNull(dataSourceIds) && dataSourceIds.length > 0) {
            Arrays.stream(dataSourceIds).forEach(id -> {
                GroupDataSource groupDataSource = new GroupDataSource();
                groupDataSource.setGroupId(group.getId());
                groupDataSource.setDatasourceId(id);
                gdService.save(groupDataSource);
            });
        }
        this.refreshRss(group.getId());
    }

    @Override
    public void refreshRss(Long groupId) {
        Group group = this.getById(groupId);
        List<Item> items = new ArrayList<>();
        List<Matcher> matchers = JSON.parseArray(group.getMatchersJson(), Matcher.class);
        ConvertContext convertContext = ConvertContext.builder()
                .groupId(group.getId())
                .referenceId(group.getReferenceId())
                .quality(group.getQuality())
                .language(group.getLanguage())
                .matchers(matchers)
                .build();

        List<GroupDataSource> referenceDataSources = gdService.getByGroupId(group.getId());
        for (GroupDataSource ds : referenceDataSources) {
            String rss = rssLoader.loadFromFile(getDatasourceRssFilepath(ds.getDatasourceId()));
            SyndFeed syndFeed = FeedUtils.getFeed(rss);
            List<SyndEntry> entryList = FeedUtils.getEntries(syndFeed);
            if (!CollectionUtils.isEmpty(entryList)) {
                for (SyndEntry entry : entryList) {
                    Item item = rssConvertor.convert(entry, convertContext);
                    if (Objects.nonNull(item)) {
                        items.add(item);
                    }
                }
            }
        }
        String xml = rssConvertor.combine(items, group.getId());
        rssWriter.write(xml, getGroupRssFilepath(group.getId()));
    }

    @Override
    public String getRss(String filename) {
        return rssLoader.loadFromFile(getGroupRssFilepath(filename));
    }

    @Override
    public String getAllRss() {
        List<Group> groupList = this.findAll();
        List<SyndEntry> entryList = new ArrayList<>();
        String mappingUrl = settingsManager.get().getBasic().getMappingUrl();
        for (Group group : groupList) {
            String rss = rssLoader.loadFromFile(getGroupRssFilepath(group.getId()));
            SyndFeed feed = FeedUtils.getFeed(rss);
            entryList.addAll(FeedUtils.getEntries(feed));
        }

        // 先将分组转换的结果，按照发布时间去重，保留最新发布的
        entryList = this.distinctByPubDate(entryList);

        // 加上自动转换分组
        String autoRss = rssLoader.loadFromFile(getAutomatedGroupRssFilepath());
        SyndFeed autoFeed = FeedUtils.getFeed(autoRss);
        entryList.addAll(FeedUtils.getEntries(autoFeed));

        // 再将所有的条目去重，保留靠前的，也就是分组转换到的结果优先
        entryList = this.distinctByOrderAsc(entryList);

        SyndFeed syndFeed = FeedUtils.createFeed();
        FeedUtils.setFeedType(syndFeed, RSS_2_0);
        FeedUtils.setTitle(syndFeed, RSS_TITLE);
        FeedUtils.setLink(syndFeed, getAllGroupLink(mappingUrl));
        FeedUtils.setDescription(syndFeed, RSS_DESCRIPTION);
        FeedUtils.setEntries(syndFeed, entryList);
        return FeedUtils.writeString(syndFeed);
    }

    private List<SyndEntry> distinctByPubDate(List<SyndEntry> entryList) {
        Map<String, SyndEntry> dictionary = new LinkedHashMap<>();
        for (SyndEntry entry : entryList) {
            String key = FeedUtils.getUri(entry);
            if (!dictionary.containsKey(key)) {
                dictionary.put(key, entry);
            } else {
                SyndEntry oldEntry = dictionary.get(key);
                if (oldEntry.getPublishedDate().before(entry.getPublishedDate())) {
                    dictionary.put(key, entry);
                }
            }
        }
        return new ArrayList<>(dictionary.values());
    }

    private List<SyndEntry> distinctByOrderAsc(List<SyndEntry> entryList) {
        Map<String, SyndEntry> dictionary = new LinkedHashMap<>();
        for (SyndEntry entry : entryList) {
            String key = FeedUtils.getUri(entry);
            if (!dictionary.containsKey(key)) {
                dictionary.put(key, entry);
            }
        }
        return new ArrayList<>(dictionary.values());
    }
}
