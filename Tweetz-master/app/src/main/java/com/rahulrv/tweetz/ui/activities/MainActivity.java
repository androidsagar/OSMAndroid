package com.rahulrv.tweetz.ui.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.rahulrv.tweetz.MyApplication;
import com.rahulrv.tweetz.R;
import com.rahulrv.tweetz.api.TwitterApi;
import com.rahulrv.tweetz.databinding.ActivityMainBinding;
import com.rahulrv.tweetz.model.trends.TrendsItem;
import com.rahulrv.tweetz.ui.TrendsAdapter;
import com.rahulrv.tweetz.viewmodel.MainActivityView;
import com.rahulrv.tweetz.viewmodel.MainActivityViewModel;

import java.util.List;

import javax.inject.Inject;

/**
 * Main launcher activity of the app.
 */
public class MainActivity extends BaseActivity<ActivityMainBinding, MainActivityViewModel> implements MainActivityView {

    @Inject TwitterApi twitterApi;

    private TrendsAdapter trendsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.getComponent().inject(this);

        // TODO Inject using Dagger 2
        viewModel = new MainActivityViewModel(twitterApi);
        viewModel.attach(this);

        bindView(R.layout.activity_main);
        setActionBar(binding.toolbar);
        binding.setIsLoading(true);

        trendsAdapter = new TrendsAdapter();
        binding.trends.setAdapter(trendsAdapter);
    }

    @Override protected void onStart() {
        super.onStart();
        // hard code san francisco for now
        viewModel.fetchTrends("2487956");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override public void load(List<TrendsItem> items) {
        binding.setIsLoading(false);
        trendsAdapter.setTrendsItems(items);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                View searchMenuView = binding.toolbar.findViewById(R.id.menu_search);
                Bundle options = ActivityOptions.makeSceneTransitionAnimation(this, searchMenuView,
                        getString(R.string.transition_search_back)).toBundle();
                startActivityForResult(new Intent(this, SearchActivity.class), 0, options);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
