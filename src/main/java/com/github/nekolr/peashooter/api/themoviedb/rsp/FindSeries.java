package com.github.nekolr.peashooter.api.themoviedb.rsp;

import java.util.List;

public record FindSeries(List<TvResult> tv_results) {

    public record TvResult(String name, String original_name) {}
}
