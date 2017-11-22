package com.rahulrv.tweetz.viewmodel;

import com.rahulrv.tweetz.model.search.StatusesItem;

import java.util.List;

/**
 *
 *
 */

public interface SearchActivityView extends IView {

    void showSearchResult(List<StatusesItem> statusesItems);

}
