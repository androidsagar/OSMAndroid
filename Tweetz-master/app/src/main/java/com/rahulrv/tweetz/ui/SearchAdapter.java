package com.rahulrv.tweetz.ui;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.rahulrv.tweetz.R;
import com.rahulrv.tweetz.databinding.SearchRowLayoutBinding;
import com.rahulrv.tweetz.model.search.StatusesItem;

import java.util.Collections;
import java.util.List;

/**
 *
 *
 */

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private List<StatusesItem> statusesItems = Collections.emptyList();

    public SearchAdapter(final List<StatusesItem> items) {
        this.statusesItems = items;
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        SearchRowLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.search_row_layout, parent, false);
        return new SearchViewHolder(binding);
    }

    @Override public void onBindViewHolder(SearchViewHolder holder, int position) {
        holder.update(statusesItems.get(position));
        holder.itemView.setOnClickListener(view -> {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            builder.setToolbarColor(ContextCompat.getColor(view.getContext(), R.color.background_dark));
//            builder.setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left);
//            builder.setExitAnimations(this, R.anim.slide_in_left, R.anim.slide_out_right);
            //customTabsIntent.launchUrl(view.getContext(), Uri.parse(statusesItems.get(position).));
        });
    }

    @Override public int getItemCount() {
        return statusesItems.size();
    }

    public void setStatusesItems(List<StatusesItem> statusesItems) {
        this.statusesItems = statusesItems;
        notifyDataSetChanged();
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {

        SearchRowLayoutBinding binding;

        public SearchViewHolder(SearchRowLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void update(StatusesItem searchItem) {
            binding.setSearchItem(searchItem);
            binding.executePendingBindings();
        }
    }
}