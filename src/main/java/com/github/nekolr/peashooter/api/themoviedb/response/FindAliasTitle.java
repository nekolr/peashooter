package com.github.nekolr.peashooter.api.themoviedb.response;

import java.util.List;

public record FindAliasTitle(List<Title> results) {

    public record Title(String iso_3166_1, String title, String type) {
    }
}
