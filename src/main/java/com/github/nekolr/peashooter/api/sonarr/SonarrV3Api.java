package com.github.nekolr.peashooter.api.sonarr;


import com.github.nekolr.peashooter.api.sonarr.req.AddRssIndexer;

public interface SonarrV3Api {

    String ADD_INDEXER_URI = "/api/v3/indexer";

    Boolean addRssIndexer(AddRssIndexer indexer);
}
