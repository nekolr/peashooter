package com.github.nekolr.peashooter.api.sonarr;


import com.github.nekolr.peashooter.api.sonarr.req.AddRssIndexer;
import com.github.nekolr.peashooter.api.sonarr.rsp.Queue;
import com.github.nekolr.peashooter.api.sonarr.rsp.Series;
import com.github.nekolr.peashooter.api.sonarr.rsp.Status;

import java.util.List;

public interface SonarrV3Api {

    String X_API_KEY_HEADER_NAME = "X-Api-Key";

    String ADD_INDEXER_URI = "/api/v3/indexer";

    String GET_STATUS_URI = "/api/v3/system/status";

    String GET_SERIES_LIST_URI = "/api/v3/series";

    String GET_SERIES_URI = "/api/v3/series/{0}";

    String GET_QUEUE_LIST_URI = "/api/v3/queue";

    List<Queue> getQueueList();

    Boolean addRssIndexer(AddRssIndexer indexer);

    Status getStatus();

    List<Series> getSeriesList();

    Series getSeries(String id);
}
