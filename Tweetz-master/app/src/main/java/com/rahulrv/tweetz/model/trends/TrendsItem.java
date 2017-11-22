package com.rahulrv.tweetz.model.trends;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class TrendsItem{

//	@SerializedName("promoted_content")
//	public abstract Object promotedContent();

//	@SerializedName("tweet_volume")
//	public abstract int tweetVolume();

	@SerializedName("query")
	public abstract String query();

	@SerializedName("name")
	public abstract String name();

	@SerializedName("url")
	public abstract String url();

	public static TypeAdapter<TrendsItem> typeAdapter(Gson gson) {
		return new AutoValue_TrendsItem.GsonTypeAdapter(gson);
	}
}