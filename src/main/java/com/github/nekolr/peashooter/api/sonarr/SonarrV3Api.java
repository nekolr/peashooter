package com.github.nekolr.peashooter.api.sonarr;


import com.github.nekolr.peashooter.api.sonarr.req.AddNotification;
import com.github.nekolr.peashooter.api.sonarr.req.AddRssIndexer;
import com.github.nekolr.peashooter.api.sonarr.rsp.Notification;
import com.github.nekolr.peashooter.api.sonarr.rsp.Queue;
import com.github.nekolr.peashooter.api.sonarr.rsp.Series;
import com.github.nekolr.peashooter.api.sonarr.rsp.Status;

import java.util.List;

public interface SonarrV3Api {

    String X_API_KEY_HEADER_NAME = "X-Api-Key";

    String ADD_INDEXER_URI = "/api/v3/indexer";

    String ADD_NOTIFICATION_URI = "/api/v3/notification";

    String GET_STATUS_URI = "/api/v3/system/status";

    String GET_SERIES_LIST_URI = "/api/v3/series";

    String GET_SERIES_URI = "/api/v3/series/{0}";

    String GET_QUEUE_LIST_URI = "/api/v3/queue";

    String GET_NOTIFICATION_URI = "/api/v3/notification";

    List<Queue> getQueueList();

    Boolean addRssIndexer(AddRssIndexer indexer);

    List<Notification> getNotifications();

    void addNotification(AddNotification notification);

    Status getStatus();

    List<Series> getSeriesList();

    Series getSeries(String id);
}
