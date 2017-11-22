package com.rahulrv.tweetz.ui.activities;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.widget.Toast;

import com.rahulrv.tweetz.viewmodel.BaseViewModel;
import com.rahulrv.tweetz.viewmodel.IView;

/**
 *
 *
 */

public abstract class BaseActivity<B extends ViewDataBinding, T extends BaseViewModel> extends Activity implements IView {

    protected T viewModel;
    B binding;

    /**
     * ViewModel must be initialized before bindView() is called
     * @param layout layout for the activity
     */
    protected final void bindView(int layout) {
        if (viewModel == null) {
            throw new IllegalStateException("viewModel must not be null and should be injected via activityComponent().inject(this)");
        }
        binding = DataBindingUtil.setContentView(this, layout);
    }

    @Override protected void onStop() {
        super.onStop();
        viewModel.clearSubscriptions();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        viewModel.detach();
    }

    @Override public void error(Throwable e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override public void error() {
        Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
    }
}
