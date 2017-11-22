package com.rahulrv.tweetz.utils;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 *
 *
 */

public class CustomBindingAdapters {

    @BindingAdapter({"profile"})
    public static void loadImage(ImageView view, String url) {
        Picasso.with(view.getContext())
                .load(url)
                .transform(new CircleTransformation())
                .into(view);
    }
}
