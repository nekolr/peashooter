package com.github.nekolr.peashooter.api.themoviedb.rsp;

import java.util.List;

public record FindById(List<TvResult> tv_results) {

    public record TvResult(Integer id, String name, String original_name) {}
}
