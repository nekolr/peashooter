package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.entity.domain.SeriesName;
import com.github.nekolr.peashooter.repository.SeriesNameRepository;
import com.github.nekolr.peashooter.service.ISeriesNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;


@Service
@CacheConfig(cacheNames = "seriesName")
@RequiredArgsConstructor
public class SeriesNameServiceImpl implements ISeriesNameService {

    private final SeriesNameRepository seriesNameRepository;

    @Override
    public List<SeriesName> findAll() {
        return seriesNameRepository.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {@CacheEvict(key = "#p0.titleEn", condition = "#p0.titleEn != null")})
    public void saveSeriesName(SeriesName seriesName) {
        if (StringUtils.hasText(seriesName.getTitleEn())) {
            // 写库前先按英文标题查重，避免重复插入
            SeriesName existing = seriesNameRepository.findByTitleEn(seriesName.getTitleEn()).orElse(null);
            if (Objects.nonNull(existing)) {
                existing.setTitleZhCN(seriesName.getTitleZhCN());
                seriesNameRepository.save(existing);
                return;
            }
        }
        seriesNameRepository.save(seriesName);
    }

    @Override
    @Cacheable(key = "#p0", unless = "null == #result")
    public SeriesName findByTitleEn(String titleEn) {
        // 精确匹配，语义上就应该精确查询
        return seriesNameRepository.findByTitleEn(titleEn).orElse(null);
    }
}
