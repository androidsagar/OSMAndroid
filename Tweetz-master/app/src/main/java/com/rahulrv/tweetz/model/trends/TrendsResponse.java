package com.rahulrv.tweetz.model.trends;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@AutoValue
public abstract class TrendsResponse{

	@SerializedName("as_of")
	public abstract String asOf();

	@SerializedName("created_at")
	public abstract String createdAt();

	@SerializedName("locations")
	public abstract List<LocationsItem> locations();

	@SerializedName("trends")
	public abstract List<TrendsItem> trends();

	public static TypeAdapter<TrendsResponse> typeAdapter(Gson gson) {
		return new AutoValue_TrendsResponse.GsonTypeAdapter(gson);
	}
}