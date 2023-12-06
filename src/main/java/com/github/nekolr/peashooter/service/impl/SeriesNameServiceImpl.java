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
import java.util.Optional;


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
        List<SeriesName> seriesNames = this.findAll();
        Optional<SeriesName> op = seriesNames.stream()
                .filter(sn -> sn.getTitleJp().equals(seriesName.getTitleJp())).findAny();
        if (!op.isPresent()) {
            seriesNameRepository.save(seriesName);
        }
    }

    @Override
    public SeriesName findByTitleJp(String titleJp) {
        SeriesName seriesName = new SeriesName();
        seriesName.setTitleJp(titleJp);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withNullHandler(ExampleMatcher.NullHandler.IGNORE)
                .withMatcher("title_jp", ExampleMatcher.GenericPropertyMatcher.of(ExampleMatcher.StringMatcher.CONTAINING));
        return seriesNameRepository.findOne(Example.of(seriesName, matcher)).orElse(null);
    }
}
