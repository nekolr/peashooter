package com.github.nekolr.peashooter.api.qb;

import com.github.nekolr.peashooter.api.qb.rsp.AppVersion;
import com.github.nekolr.peashooter.api.qb.rsp.SID;

public interface QBittorrentApi {

    String LOGIN_URI = "/api/v2/auth/login";
    String APP_VERSION_URI = "/api/v2/app/version";
    String RENAME_TORRENT_URI = "/api/v2/torrents/rename";

    SID login();

    AppVersion getAppVersion();

    boolean renameTorrent(String hash, String name);
}
