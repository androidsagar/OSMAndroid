package com.rahulrv.tweetz.model.trends;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class LocationsItem{

	@SerializedName("name")
	public abstract String name();

	@SerializedName("woeid")
	public abstract int woeid();

	public static TypeAdapter<LocationsItem> typeAdapter(Gson gson) {
		return new AutoValue_LocationsItem.GsonTypeAdapter(gson);
	}
}