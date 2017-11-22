package com.rahulrv.tweetz.api;

import com.rahulrv.tweetz.model.search.SearchResponse;
import com.rahulrv.tweetz.model.trends.TrendsResponse;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 *
 *
 */

public interface TwitterApi {

    @GET("search/tweets.json")
    Flowable<SearchResponse> searchTweets(@Query("q") String query);

    @GET("trends/place.json")
    Observable<List<TrendsResponse>> getTrends(@Query("id") String placeId);
}
