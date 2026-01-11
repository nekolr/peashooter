package com.github.nekolr.peashooter.api.qb;

import com.github.nekolr.peashooter.api.qb.response.AppVersion;
import com.github.nekolr.peashooter.api.qb.response.SID;

import java.util.Optional;

public interface QBittorrentApi {

    String LOGIN_URI = "/api/v2/auth/login";
    String APP_VERSION_URI = "/api/v2/app/version";
    String RENAME_TORRENT_URI = "/api/v2/torrents/rename";

    /**
     * 登录
     */
    Optional<SID> login();

    /**
     * 获取 qBittorrent 的版本
     */
    Optional<AppVersion> getAppVersion();

    /**
     * 种子重命名
     *
     * @param hash 种子 hash
     * @param name 新名称
     */
    boolean renameTorrent(String hash, String name);
}
