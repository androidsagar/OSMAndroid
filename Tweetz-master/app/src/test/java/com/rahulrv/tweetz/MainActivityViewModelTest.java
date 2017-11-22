package com.rahulrv.tweetz;

import com.rahulrv.tweetz.api.TwitterApi;
import com.rahulrv.tweetz.model.trends.LocationsItem;
import com.rahulrv.tweetz.model.trends.TrendsItem;
import com.rahulrv.tweetz.model.trends.TrendsResponse;
import com.rahulrv.tweetz.viewmodel.MainActivityView;
import com.rahulrv.tweetz.viewmodel.MainActivityViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 *
 */
@RunWith(PowerMockRunner.class)
public class MainActivityViewModelTest {
    private static String SF_LOC_ID = "2487956";

    @Rule RxSchedulersOverrideRule rxSchedulersOverrideRule = new RxSchedulersOverrideRule();

    @Mock MainActivityView mainActivityView;
    @Mock TwitterApi twitterApi;

    private MainActivityViewModel mainActivityViewModel;

    @Before public void setup() {
        MockitoAnnotations.initMocks(this);
        mainActivityViewModel = new MainActivityViewModel(twitterApi);
        mainActivityViewModel.attach(mainActivityView);
    }

    @Test public void loadTrends() {
        List<TrendsResponse> countryList = new ArrayList<>();
        countryList.add(new TrendsResponse() {
            @Override public String asOf() {
                return null;
            }

            @Override public String createdAt() {
                return null;
            }

            @Override public List<LocationsItem> locations() {
                return null;
            }

            @Override public List<TrendsItem> trends() {
                return Collections.emptyList();
            }
        });

        doReturn(Observable.just(countryList)).when(twitterApi).getTrends(SF_LOC_ID);
        mainActivityViewModel.fetchTrends(SF_LOC_ID);
        verify(mainActivityView,times(1)).load(countryList.get(0).trends());
    }

    @Test public void error_loadTrends() {
        Throwable throwable = new Throwable();
        doReturn(Observable.error(throwable)).when(twitterApi).getTrends(SF_LOC_ID);
        mainActivityViewModel.fetchTrends(SF_LOC_ID);
        verify(mainActivityView,times(1)).error(throwable);
    }
}
