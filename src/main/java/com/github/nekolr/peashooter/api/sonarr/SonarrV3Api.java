package com.github.nekolr.peashooter.api.sonarr;


import com.github.nekolr.peashooter.api.sonarr.request.AddWebhookNotification;
import com.github.nekolr.peashooter.api.sonarr.request.AddRssIndexer;
import com.github.nekolr.peashooter.api.sonarr.response.Notification;
import com.github.nekolr.peashooter.api.sonarr.response.Queue;
import com.github.nekolr.peashooter.api.sonarr.response.Series;
import com.github.nekolr.peashooter.api.sonarr.response.Status;

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

    /**
     * 获取队列
     */
    List<Queue> getQueueList();

    /**
     * 添加 rss 索引器
     */
    Boolean addRssIndexer(AddRssIndexer indexer);

    /**
     * 获取所有的通知
     */
    List<Notification> getNotifications();

    /**
     * 添加 webhook 通知
     */
    void addWebhookNotification(AddWebhookNotification notification);

    /**
     * 获取 sonarr 状态
     */
    Status getStatus();

    /**
     * 获取剧集列表
     */
    List<Series> getSeriesList();

    /**
     * 获取剧集
     */
    Series getSeries(String id);
}
