package com.github.nekolr.peashooter.api.sonarr;

import com.github.nekolr.peashooter.api.sonarr.rsp.Queue;
import com.github.nekolr.peashooter.api.sonarr.rsp.Series;
import com.github.nekolr.peashooter.api.sonarr.rsp.Status;
import org.springframework.cache.annotation.*;

import java.util.List;

@CacheConfig(cacheNames = "sonarr")
public interface SonarrApi {

    String GET_QUEUE_LIST_URI = "/api/queue";
    String GET_STATUS_URI = "/api/system/status";
    String GET_SERIES_LIST_URI = "/api/series";
    String GET_SERIES_URI = "/api/series/{0}";

    Status getStatus();

    List<Queue> getQueueList();

    @Cacheable(key = "#p0")
    Series getSeries(String id);

    @Cacheable(key = "'series'")
    List<Series> getSeriesList();

    @Caching(evict = {@CacheEvict(key = "'series'", beforeInvocation = true)}, put = {@CachePut(key = "'series'")})
    List<Series> refreshSeriesList();
}
