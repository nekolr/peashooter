package com.github.nekolr.peashooter.api.themoviedb;

import com.github.nekolr.peashooter.api.themoviedb.rsp.FindAliasTitle;
import com.github.nekolr.peashooter.api.themoviedb.rsp.FindById;
import com.github.nekolr.peashooter.api.themoviedb.rsp.FindByKeyword;

import java.util.List;

public interface TheMovieDbApi {

    String THE_MOVIE_DB_HOST = "https://api.themoviedb.org";

    String FIND_BY_ID_URI = "/3/find/{0}";

    String FIND_BY_KEYWORD_URI = "/3/search/tv?query={0}&page=1&include_adult=false&language=en-US";

    String FIND_ALIAS_TITLE_URI = "/3/tv/{0}/alternative_titles";

    FindById.TvResult findByImdbId(String imdbId);

    FindById.TvResult findByTvdbId(String tvdbId);

    FindByKeyword.TvResult findByKeyword(String keyword);

    List<FindAliasTitle.Title> findAliasTitles(Integer seriesId);
}
