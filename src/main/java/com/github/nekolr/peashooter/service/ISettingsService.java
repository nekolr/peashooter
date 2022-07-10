package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.controller.req.settings.*;

public interface ISettingsService {

    boolean testSonarr();

    boolean testQb();

    void setBasic(SetBasic setting);

    void setProxy(SetHttpProxy setting);

    void setSonarr(SetSonarr setting);

    void setQbittorrent(SetQbittorrent setting);
}
