package com.rahulrv.tweetz.ui;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.rahulrv.tweetz.R;
import com.rahulrv.tweetz.databinding.TrendsRowLayoutBinding;
import com.rahulrv.tweetz.model.trends.TrendsItem;

import java.util.List;

/**
 *
 *
 */

public class TrendsAdapter extends RecyclerView.Adapter<TrendsAdapter.TrendsViewHolder> {

    private List<TrendsItem> trendsItems;

    @Override
    public TrendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TrendsRowLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.trends_row_layout, parent, false);
        return new TrendsViewHolder(binding);
    }

    @Override public void onBindViewHolder(TrendsViewHolder holder, int position) {
        holder.update(trendsItems.get(position));
        holder.itemView.setOnClickListener(view -> {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            builder.setToolbarColor(ContextCompat.getColor(view.getContext(), R.color.background_dark));
//            builder.setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left);
//            builder.setExitAnimations(this, R.anim.slide_in_left, R.anim.slide_out_right);
            customTabsIntent.launchUrl(view.getContext(), Uri.parse(trendsItems.get(position).url()));
        });
    }

    @Override public int getItemCount() {
        return trendsItems.size();
    }


    public void setTrendsItems(List<TrendsItem> trendsItems) {
        this.trendsItems = trendsItems;
        notifyDataSetChanged();
    }

    public static class TrendsViewHolder extends RecyclerView.ViewHolder {

        TrendsRowLayoutBinding binding;

        public TrendsViewHolder(TrendsRowLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void update(TrendsItem trendsItem) {
            binding.setTrend(trendsItem);
            binding.executePendingBindings();
        }
    }
}
