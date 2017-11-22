package com.rahulrv.tweetz.viewmodel;

import com.rahulrv.tweetz.model.trends.TrendsItem;

import java.util.List;

/**
 *
 *
 */

public interface MainActivityView extends IView {

    void load(List<TrendsItem> items);

}
