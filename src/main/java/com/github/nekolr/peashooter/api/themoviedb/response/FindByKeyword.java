package com.github.nekolr.peashooter.api.themoviedb.response;

import java.util.List;

public record FindByKeyword(List<TvResult> results) {

    public record TvResult(Integer id, String name, String[] genre_ids) {

    }
}
