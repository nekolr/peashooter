package com.github.nekolr.peashooter.service.impl;

import com.alibaba.fastjson2.JSON;
import com.github.nekolr.peashooter.config.SettingsManager;
import com.github.nekolr.peashooter.constant.Peashooter;
import com.github.nekolr.peashooter.controller.req.group.SaveGroup;
import com.github.nekolr.peashooter.controller.req.group.GetGroupList;
import com.github.nekolr.peashooter.entity.domain.Group;
import com.github.nekolr.peashooter.entity.domain.GroupDataSource;
import com.github.nekolr.peashooter.entity.mapper.GroupMapper;
import com.github.nekolr.peashooter.repository.GroupRepository;
import com.github.nekolr.peashooter.rss.Item;
import com.github.nekolr.peashooter.rss.convert.ConvertContext;
import com.github.nekolr.peashooter.rss.convert.Matcher;
import com.github.nekolr.peashooter.rss.convert.RssConvertor;
import com.github.nekolr.peashooter.rss.load.RssLoader;
import com.github.nekolr.peashooter.rss.write.RssWriter;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.nekolr.peashooter.constant.Peashooter.getRssFilepath;

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
        List<GroupDataSource> gdList = gdService.getByGroupId(group.getId());
        List<Long> dsList = gdList.stream().map(GroupDataSource::getDatasourceId).collect(Collectors.toList());
        group.setDataSourceIds(dsList.toArray(new Long[dsList.size()]));
        group.setMatchers(JSON.parseArray(group.getMatchersJson(), Matcher.class));
        return group;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Group save(Group group) {
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
        list.stream().forEach(e -> e.setRssLink(Peashooter.getGroupLink(settingsManager.get().getBasic().getMappingUrl(), e.getId())));

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
        List<Item> items = new ArrayList<>();
        Group group = this.getById(groupId);

        String quality = group.getQuality();
        String language = group.getLanguage();
        String referenceId = group.getReferenceId();
        List<Matcher> matchers = JSON.parseArray(group.getMatchersJson(), Matcher.class);
        ConvertContext convertContext = new ConvertContext(group.getId(), referenceId, quality, language, matchers);

        List<GroupDataSource> referenceDataSources = gdService.getByGroupId(group.getId());
        for (GroupDataSource ds : referenceDataSources) {
            String rss = rssLoader.loadFromFile(getRssFilepath(ds.getDatasourceId(), false));
            SyndFeed syndFeed = FeedUtils.getFeed(rss);
            List<SyndEntry> entryList = FeedUtils.getEntries(syndFeed);
            for (SyndEntry entry : entryList) {
                Item item = rssConvertor.convert(entry, convertContext);
                if (Objects.nonNull(item)) {
                    items.add(item);
                }
            }
        }
        String xml = rssConvertor.convert(items, group.getId());
        rssWriter.write(xml, getRssFilepath(group.getId(), true));
    }

    @Override
    public String getRss(String filename) {
        return rssLoader.loadFromFile(getRssFilepath(filename, true));
    }
}
