package com.github.nekolr.peashooter.api.themoviedb.rsp;

import java.util.List;

public record FindByKeyword(List<TvResult> results) {

    public record TvResult(Integer id, String name, String[] genre_ids) {

    }
}
