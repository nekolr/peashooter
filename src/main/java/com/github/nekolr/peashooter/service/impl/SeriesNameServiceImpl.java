package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.entity.domain.SeriesName;
import com.github.nekolr.peashooter.repository.SeriesNameRepository;
import com.github.nekolr.peashooter.service.ISeriesNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SeriesNameServiceImpl implements ISeriesNameService {

    private final SeriesNameRepository seriesNameRepository;

    @Override
    public List<SeriesName> findAll() {
        return seriesNameRepository.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSeriesName(SeriesName seriesName) {
        seriesNameRepository.save(seriesName);
    }

    @Override
    public SeriesName findByTitleEn(String titleEn) {
        SeriesName seriesName = new SeriesName();
        seriesName.setTitleEn(titleEn);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withNullHandler(ExampleMatcher.NullHandler.IGNORE)
                .withMatcher("title_en", ExampleMatcher.GenericPropertyMatcher.of(ExampleMatcher.StringMatcher.CONTAINING));
        return seriesNameRepository.findOne(Example.of(seriesName, matcher)).orElse(null);
    }
}
