package com.github.nekolr.peashooter.rss.convert.resolver;

import lombok.Getter;

@Getter
public enum PubDateType {

    DEFAULT("default"),
    MI_KAN("mi-kan");

    private String type;

    PubDateType(String type) {
        this.type = type;
    }
}
