package com.github.nekolr.peashooter.api.themoviedb;

import com.github.nekolr.peashooter.api.themoviedb.rsp.FindAliasTitle;
import com.github.nekolr.peashooter.api.themoviedb.rsp.FindById;

import java.util.List;
import java.util.Optional;

public interface TheMovieDbApi {

    String THE_MOVIE_DB_HOST = "https://api.themoviedb.org";

    String FIND_BY_ID_URI = "/3/find/{0}";

    String FIND_ALIAS_TITLE_URI = "/3/tv/{0}/alternative_titles";

    Optional<FindById.TvResult> findByImdbId(String imdbId);

    Optional<FindById.TvResult> findByTvdbId(String tvdbId);

    List<FindAliasTitle.Title> findAliasTitles(Integer seriesId);
}
