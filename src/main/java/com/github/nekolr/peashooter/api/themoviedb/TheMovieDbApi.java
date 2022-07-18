package com.github.nekolr.peashooter.api.themoviedb;

import com.github.nekolr.peashooter.api.themoviedb.rsp.FindSeries.TvResult;

public interface TheMovieDbApi {

    String THE_MOVIE_DB_HOST = "https://api.themoviedb.org";

    String FIND_SERIES_URI = "/3/find/{0}";

    TvResult findByImdbId(String imdbId);

    TvResult findByTvdbId(String tvdbId);
}
