package com.rahulrv.tweetz.viewmodel;

import android.util.Log;

import com.rahulrv.tweetz.api.TwitterApi;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 *
 *
 */

public class SearchActivityViewModel extends BaseViewModel<SearchActivityView> {

    private TwitterApi twitterApi;
    private Disposable searchDisposable;

    public SearchActivityViewModel(TwitterApi twitterApi) {
        this.twitterApi = twitterApi;
    }

    public void searchTweets(String searchString) {
        if (searchDisposable != null) {
            searchDisposable.dispose();
        }
        searchDisposable = twitterApi.searchTweets(searchString)
                .delay(600, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(searchResponse -> {
                    Log.d("search", searchString);
                    view.showSearchResult(searchResponse.items());
                }, throwable -> view.error(throwable));

    }

}
