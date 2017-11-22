package com.rahulrv.tweetz.ui.activities;

import android.app.SharedElementCallback;
import android.graphics.Point;
import android.os.Bundle;
import android.transition.TransitionSet;
import android.view.View;
import android.widget.SearchView;

import com.rahulrv.tweetz.MyApplication;
import com.rahulrv.tweetz.R;
import com.rahulrv.tweetz.api.TwitterApi;
import com.rahulrv.tweetz.databinding.ActivitySearchBinding;
import com.rahulrv.tweetz.model.search.StatusesItem;
import com.rahulrv.tweetz.ui.SearchAdapter;
import com.rahulrv.tweetz.ui.transitions.CircularReveal;
import com.rahulrv.tweetz.utils.ImeUtils;
import com.rahulrv.tweetz.utils.TransitionUtils;
import com.rahulrv.tweetz.viewmodel.SearchActivityView;
import com.rahulrv.tweetz.viewmodel.SearchActivityViewModel;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class SearchActivity extends BaseActivity<ActivitySearchBinding, SearchActivityViewModel> implements SearchActivityView {

    @Inject TwitterApi twitterApi;

    private SearchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.getComponent().inject(this);

        // TODO Inject using Dagger2
        viewModel = new SearchActivityViewModel(twitterApi);
        viewModel.attach(this);

        bindView(R.layout.activity_search);
        setupTransitions();
        binding.searchback.setOnClickListener(view -> finishAfterTransition());
        adapter = new SearchAdapter(Collections.emptyList());
        binding.searchResults.setAdapter(adapter);

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String s) {
                viewModel.searchTweets(s);
                return true;
            }

            @Override public boolean onQueryTextChange(String s) {
                if (s.length() < 3) {
                    return false;
                }
                viewModel.searchTweets(s);
                return true;
            }
        });
    }

    @Override protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        // needed to suppress the default window animation when closing the activity
        overridePendingTransition(0, 0);
        super.onPause();
    }

    @Override public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        // focus the search view once the enter transition finishes
        binding.searchView.requestFocus();
        ImeUtils.showIme(binding.searchView);
    }

    private void setupTransitions() {
        // grab the position that the search icon transitions in *from*
        // & use it to configure the return transition
        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(
                    List<String> sharedElementNames,
                    List<View> sharedElements,
                    List<View> sharedElementSnapshots) {
                if (sharedElements != null && !sharedElements.isEmpty()) {
                    View searchIcon = sharedElements.get(0);
                    if (searchIcon.getId() != R.id.searchback) return;
                    int centerX = (searchIcon.getLeft() + searchIcon.getRight()) / 2;
                    CircularReveal hideResults = (CircularReveal) TransitionUtils.findTransition(
                            (TransitionSet) getWindow().getReturnTransition(),
                            CircularReveal.class, R.id.results_container);
                    if (hideResults != null) {
                        hideResults.setCenter(new Point(centerX, 0));
                    }
                }
            }
        });
    }

    @Override public void showSearchResult(List<StatusesItem> statusesItems) {
        if (statusesItems.isEmpty()) {
            binding.searchResults.setVisibility(View.GONE);
            binding.stubNoSearchResults.setVisibility(View.VISIBLE);
        } else {
            binding.searchResults.setVisibility(View.VISIBLE);
            binding.stubNoSearchResults.setVisibility(View.GONE);
        }
        adapter.setStatusesItems(statusesItems);
    }
}
