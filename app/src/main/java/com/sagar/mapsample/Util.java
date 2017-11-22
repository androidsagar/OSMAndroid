package com.sagar.mapsample;

/**
 * Created by sagar on 19/11/17.
 */

public class Util {

    public String getUrl(String buildingDetails){
        String and="&";
        String nomination="http://nominatim.openstreetmap.org/search/";
        String query="q=".concat(buildingDetails.trim().replace(" ","+"));
        String format="format=json";
        String polygon="polygon=1";
        String addressDetails="addressdetails=1";
        return nomination.concat(format).concat(and).concat(query).concat(and).concat(polygon).concat(and).concat(addressDetails);
    }
}
