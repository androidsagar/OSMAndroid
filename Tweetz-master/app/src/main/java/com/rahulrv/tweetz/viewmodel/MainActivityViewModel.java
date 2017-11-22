package com.rahulrv.tweetz.viewmodel;

import com.rahulrv.tweetz.api.TwitterApi;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * ViewModel for MainActivity
 */

public class MainActivityViewModel extends BaseViewModel<MainActivityView> {

    private TwitterApi twitterApi;

    public MainActivityViewModel(TwitterApi twitterApi) {
        this.twitterApi = twitterApi;
    }

    public void fetchTrends(String locationId) {
        compositeDisposable.add(twitterApi.getTrends(locationId)
                .subscribeOn(Schedulers.computation())
                .map(trendsResponses -> trendsResponses.get(0))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(trendsResponse -> view.load(trendsResponse.trends()),
                        throwable -> view.error(throwable)));
    }
}
