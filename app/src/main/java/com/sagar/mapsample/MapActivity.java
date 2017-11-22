package com.sagar.mapsample;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.location.FlickrPOIProvider;
import org.osmdroid.bonuspack.location.GeoNamesPOIProvider;
import org.osmdroid.bonuspack.location.GeocoderMapzen;
import org.osmdroid.bonuspack.location.OverpassAPIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.location.PicasaPOIProvider;
import org.osmdroid.bonuspack.routing.GoogleRoadManager;
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.MapBoxTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.ManifestUtil;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.mylocation.DirectedLocationOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapActivity extends Activity implements LocationListener, View.OnClickListener {
    private ITileSource MAPBOXSATELLITELABELLED ;
    MyLocationNewOverlay mLocationOverlay;
    protected ArrayList<GeoPoint> viaPoints;
    MapView map;
    IMapController mapController;
    LocationManager locationManager;
    FloatingActionButton btnMyLocation,btnLayers,btnSearch,btnNavigation;
    Location location;
    private int checked=0;
    RadiusMarkerClusterer mPoiMarkers;
    public static ArrayList<POI> mPOIs;
    static String graphHopperApiKey;
    static String flickrApiKey;
    static String geonamesAccount;
    static String mapzenApiKey;

    static String SHARED_PREFS_APPKEY = "OSMNavigator";
    static String PREF_LOCATIONS_KEY = "PREF_LOCATIONS";
    protected static int START_INDEX=-2, DEST_INDEX=-1;
    protected GeoPoint startPoint, destinationPoint;
    protected Marker markerStart, markerDestination;
    protected DirectedLocationOverlay myLocationOverlay;
    protected FolderOverlay mItineraryMarkers;
    public static Road[] mRoads;
    protected Polygon mDestinationPolygon;
    int mWhichRouteProvider;
    protected FolderOverlay mRoadNodeMarkers;
    static final int OSRM=0, GRAPHHOPPER_FASTEST=1, GRAPHHOPPER_BICYCLE=2, GRAPHHOPPER_PEDESTRIAN=3, GOOGLE_FASTEST=4;
    protected Polyline[] mRoadOverlays;
    protected int mSelectedRoad;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();

        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_map);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission(this);
        }
        SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);

        graphHopperApiKey = ManifestUtil.retrieveKey(this, "GRAPHHOPPER_API_KEY");
        flickrApiKey = ManifestUtil.retrieveKey(this, "FLICKR_API_KEY");
        geonamesAccount = ManifestUtil.retrieveKey(this, "GEONAMES_ACCOUNT");
        mapzenApiKey = ManifestUtil.retrieveKey(this, "MAPZEN_APIKEY");
        mWhichRouteProvider = prefs.getInt("ROUTE_PROVIDER", OSRM);


        btnMyLocation = findViewById(R.id.btnMyLocation);
        btnMyLocation.setOnClickListener(this);
        btnLayers=findViewById(R.id.btnLayer);
        btnLayers.setOnClickListener(this);
        btnSearch=findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(this);
        btnNavigation=findViewById(R.id.btnNavigation);
        btnNavigation.setOnClickListener(this);
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getOverlayManager().getTilesOverlay().setColorFilter(null);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(11);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        this.mLocationOverlay.enableMyLocation();
        map.getOverlays().add(this.mLocationOverlay);
        CompassOverlay compassOverlay=new CompassOverlay(this,map);
        compassOverlay.enableCompass();
        map.getOverlays().add(compassOverlay);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                updateLocation(location);
            }
        }
        mRoadNodeMarkers = new FolderOverlay();
        mRoadNodeMarkers.setName("Route Steps");
        map.getOverlays().add(mRoadNodeMarkers);

        mItineraryMarkers = new FolderOverlay();
        mItineraryMarkers.setName(getString(R.string.itinerary_markers_title));
        map.getOverlays().add(mItineraryMarkers);

        ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(map);
        map.getOverlays().add(myScaleBarOverlay);

        MAPBOXSATELLITELABELLED = new MapBoxTileSource("MapBoxSatelliteLabelled", 1, 19, 256, ".png");
        ((MapBoxTileSource) MAPBOXSATELLITELABELLED).retrieveAccessToken(this);
        ((MapBoxTileSource) MAPBOXSATELLITELABELLED).retrieveMapBoxMapId(this);
        TileSourceFactory.addTileSource(MAPBOXSATELLITELABELLED);
        myLocationOverlay = new DirectedLocationOverlay(this);
        map.getOverlays().add(myLocationOverlay);

        mPoiMarkers = new RadiusMarkerClusterer(this);
        Drawable clusterIconD = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_poi_cluster, null);
        Bitmap clusterIcon = ((BitmapDrawable)clusterIconD).getBitmap();
        mPoiMarkers.setIcon(clusterIcon);
        mPoiMarkers.mAnchorV = Marker.ANCHOR_BOTTOM;
        mPoiMarkers.mTextAnchorU = 0.70f;
        mPoiMarkers.mTextAnchorV = 0.27f;
        mPoiMarkers.getTextPaint().setTextSize(12 * getResources().getDisplayMetrics().density);
        map.getOverlays().add(mPoiMarkers);

        if (savedInstanceState==null){
            viaPoints = new ArrayList<GeoPoint>();
        }else {
            startPoint = savedInstanceState.getParcelable("start");
            destinationPoint = savedInstanceState.getParcelable("destination");
            viaPoints = savedInstanceState.getParcelableArrayList("viapoints");
        }
    }
    @Override protected void onSaveInstanceState (Bundle outState){
        outState.putParcelable("location", myLocationOverlay.getLocation());
        outState.putParcelable("start", startPoint);
        outState.putParcelable("destination", destinationPoint);
        outState.putParcelableArrayList("viapoints", viaPoints);
        //STATIC - outState.putParcelable("road", mRoad);
        //STATIC - outState.putParcelableArrayList("poi", mPOIs);
        //STATIC - outState.putParcelable("kml", mKmlDocument);
        //STATIC - outState.putParcelable("friends", mFriends);
        onSaveInstanceState(outState);

        savePrefs();
    }
    void savePrefs(){
        SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putInt("MAP_ZOOM_LEVEL", map.getZoomLevel());
        GeoPoint c = (GeoPoint) map.getMapCenter();
        ed.putFloat("MAP_CENTER_LAT", (float)c.getLatitude());
        ed.putFloat("MAP_CENTER_LON", (float)c.getLongitude());
        MapTileProviderBase tileProvider = map.getTileProvider();
        String tileProviderName = tileProvider.getTileSource().name();
        ed.putString("TILE_PROVIDER", tileProviderName);
        ed.putInt("ROUTE_PROVIDER", mWhichRouteProvider);
        ed.apply();
    }
    @Override
    protected void onResume() {
        super.onResume();
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        requestMyLocation();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean requestPermission(Context context) {
        boolean flag = true;
        int fineLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        int extStorage = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        List<String> permissions = new ArrayList<>();
        if (fineLocation != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (coarseLocation != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (extStorage != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        String[] permis = permissions.toArray(new String[permissions.size()]);
        if (permis.length > 0) {
            flag = false;
            requestPermissions(permis, 0);
        }
        return flag;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }

    @Override
    public void onLocationChanged(Location location) {
        //updateLocation(location);
        this.location=location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public void updateLocation(Location location) {
        GeoPoint locGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
      //  mapController.setCenter(locGeoPoint);
        mapController.animateTo(locGeoPoint);
        map.invalidate();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnMyLocation:
                requestMyLocation();
                if(location!=null)
                updateLocation(location);
                break;
            case R.id.btnLayer:
                showLayerOptions();
                break;
            case R.id.btnSearch:
                showSearchDialog();
                break;
            case R.id.btnNavigation:
                showNavigationDialog();
                break;
        }
    }

    private void showNavigationDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Want to go Somewhere");
        final View dialog= LayoutInflater.from(this).inflate(R.layout.navigate_dialog,null);
        builder.setView(dialog);
        final Dialog dialog1=builder.create();
        final AutoCompleteOnPreferences editOriginDialog=dialog.findViewById(R.id.editMyLocationDialog);
        editOriginDialog.setPrefKeys(SHARED_PREFS_APPKEY, PREF_LOCATIONS_KEY);

        final AutoCompleteOnPreferences editDestinationDialog=dialog.findViewById(R.id.editDestinationDialog);
        editDestinationDialog.setPrefKeys(SHARED_PREFS_APPKEY, PREF_LOCATIONS_KEY);
        Button btnNavigate=dialog.findViewById(R.id.btnNavigateDialog);
        btnNavigate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                handleSearchButton(START_INDEX, editOriginDialog.getText().toString());
                handleSearchButton(DEST_INDEX, editDestinationDialog.getText().toString());
                dialog1.dismiss();
            }
        });
       dialog1.show();
    }

    public void handleSearchButton(int index, String locationAddress){

        if (locationAddress.equals("")){
            removePoint(index);
            map.invalidate();
            return;
        }

        Toast.makeText(this, "Searching:\n"+locationAddress, Toast.LENGTH_LONG).show();
        AutoCompleteOnPreferences.storePreference(this, locationAddress, SHARED_PREFS_APPKEY, PREF_LOCATIONS_KEY);
        new GeocodingTask().execute(locationAddress, index);
    }
    private class GeocodingTask extends AsyncTask<Object, Void, List<Address>> {
        int mIndex;
        protected List<Address> doInBackground(Object... params) {
            String locationAddress = (String)params[0];
            mIndex = (Integer)params[1];
            GeocoderMapzen geocoder = new GeocoderMapzen(mapzenApiKey);
            //geocoder.setOptions(true); //ask for enclosing polygon (if any)
            try {
                BoundingBox viewbox = map.getBoundingBox();
                List<Address> foundAdresses = geocoder.getFromLocationName(locationAddress, 1,
                        viewbox.getLatSouth(), viewbox.getLonEast(),
                        viewbox.getLatNorth(), viewbox.getLonWest(), false);

                return foundAdresses;
            } catch (Exception e) {
                return null;
            }
        }
        protected void onPostExecute(List<Address> foundAdresses) {
            if (foundAdresses == null) {
                Toast.makeText(getApplicationContext(), "Geocoding error", Toast.LENGTH_SHORT).show();
            } else if (foundAdresses.size() == 0) { //if no address found, display an error
                Toast.makeText(getApplicationContext(), "Address not found.", Toast.LENGTH_SHORT).show();
            } else {
                Address address = foundAdresses.get(0); //get first address
                String addressDisplayName = address.getExtras().getString("display_name");
                if (mIndex == START_INDEX){
                    startPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
                    markerStart = updateItineraryMarker(markerStart, startPoint, START_INDEX,
                            R.string.departure, R.drawable.marker_departure, -1, addressDisplayName);
                    map.getController().setCenter(startPoint);
                } else if (mIndex == DEST_INDEX){
                    destinationPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
                    markerDestination = updateItineraryMarker(markerDestination, destinationPoint, DEST_INDEX,
                            R.string.destination, R.drawable.marker_destination, -1, addressDisplayName);
                    map.getController().setCenter(destinationPoint);
                }
                getRoadAsync();
                //get and display enclosing polygon:
                Bundle extras = address.getExtras();
                if (extras != null && extras.containsKey("polygonpoints")){
                    ArrayList<GeoPoint> polygon = extras.getParcelableArrayList("polygonpoints");
                    //Log.d("DEBUG", "polygon:"+polygon.size());
                    updateUIWithPolygon(polygon, addressDisplayName);
                } else {
                    updateUIWithPolygon(null, "");
                }
            }
        }
    }
    public void updateUIWithPolygon(ArrayList<GeoPoint> polygon, String name){
        List<Overlay> mapOverlays = map.getOverlays();
        int location = -1;
        if (mDestinationPolygon != null)
            location = mapOverlays.indexOf(mDestinationPolygon);
        mDestinationPolygon = new Polygon();
        mDestinationPolygon.setFillColor(0x15FF0080);
        mDestinationPolygon.setStrokeColor(0x800000FF);
        mDestinationPolygon.setStrokeWidth(5.0f);
        mDestinationPolygon.setTitle(name);
        BoundingBox bb = null;
        if (polygon != null){
            mDestinationPolygon.setPoints(polygon);
            bb = BoundingBox.fromGeoPoints(polygon);
        }
        if (location != -1)
            mapOverlays.set(location, mDestinationPolygon);
        else
            mapOverlays.add(1, mDestinationPolygon); //insert just above the MapEventsOverlay.
        setViewOn(bb);
        map.invalidate();
    }
    void setViewOn(BoundingBox bb){
        if (bb != null){
            map.zoomToBoundingBox(bb, true);
        }
    }


    public void removePoint(int index){
        if (index == START_INDEX){
            startPoint = null;
            if (markerStart != null){
                markerStart.closeInfoWindow();
                mItineraryMarkers.remove(markerStart);
                markerStart = null;
            }
        } else if (index == DEST_INDEX){
            destinationPoint = null;
            if (markerDestination != null){
                markerDestination.closeInfoWindow();
                mItineraryMarkers.remove(markerDestination);
                markerDestination = null;
            }
        } else {
            viaPoints.remove(index);
            updateUIWithItineraryMarkers();
        }
        getRoadAsync();
    }

    public void updateUIWithItineraryMarkers(){
        mItineraryMarkers.closeAllInfoWindows();
        mItineraryMarkers.getItems().clear();
        //Start marker:
        if (startPoint != null){
            markerStart = updateItineraryMarker(null, startPoint, START_INDEX,
                    R.string.departure, R.drawable.marker_departure, -1, null);
        }
        //Via-points markers if any:
        for (int index=0; index<viaPoints.size(); index++){
            updateItineraryMarker(null, viaPoints.get(index), index,
                    R.string.viapoint, R.drawable.marker_via, -1, null);
        }
        //Destination marker if any:
        if (destinationPoint != null){
            markerDestination = updateItineraryMarker(null, destinationPoint, DEST_INDEX,
                    R.string.destination, R.drawable.marker_destination, -1, null);
        }
    }
    final OnItineraryMarkerDragListener mItineraryListener = new OnItineraryMarkerDragListener();
    class OnItineraryMarkerDragListener implements Marker.OnMarkerDragListener {
        @Override public void onMarkerDrag(Marker marker) {}
        @Override public void onMarkerDragEnd(Marker marker) {
            int index = (Integer)marker.getRelatedObject();
            if (index == START_INDEX)
                startPoint = marker.getPosition();
            else if (index == DEST_INDEX)
                destinationPoint = marker.getPosition();
            else
                viaPoints.set(index, marker.getPosition());
            //update location:
            new ReverseGeocodingTask().execute(marker);
            //update route:
            getRoadAsync();
        }

        @Override
        public void onMarkerDragStart(Marker marker) {
        }
    }

    public void getRoadAsync(){
        mRoads = null;
        GeoPoint roadStartPoint = null;
        if (startPoint != null){
            roadStartPoint = startPoint;
        } else if (myLocationOverlay.isEnabled() && myLocationOverlay.getLocation() != null){
            //use my current location as itinerary start point:
            roadStartPoint = myLocationOverlay.getLocation();
        }
        if (roadStartPoint == null || destinationPoint == null){
            updateUIWithRoads(mRoads);
            return;
        }
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>(2);
        waypoints.add(roadStartPoint);
        //add intermediate via points:
        for (GeoPoint p:viaPoints){
            waypoints.add(p);
        }
        waypoints.add(destinationPoint);
        new UpdateRoadTask(this).execute(waypoints);
    }

    void updateUIWithRoads(Road[] roads){
        mRoadNodeMarkers.getItems().clear();
        List<Overlay> mapOverlays = map.getOverlays();
        if (mRoadOverlays != null){
            for (int i=0; i<mRoadOverlays.length; i++)
                mapOverlays.remove(mRoadOverlays[i]);
            mRoadOverlays = null;
        }
        if (roads == null)
            return;
        if (roads[0].mStatus == Road.STATUS_TECHNICAL_ISSUE)
            Toast.makeText(map.getContext(), "Technical issue when getting the route", Toast.LENGTH_SHORT).show();
        else if (roads[0].mStatus > Road.STATUS_TECHNICAL_ISSUE) //functional issues
            Toast.makeText(map.getContext(), "No possible route here", Toast.LENGTH_SHORT).show();
        mRoadOverlays = new Polyline[roads.length];
        for (int i=0; i<roads.length; i++) {
            Polyline roadPolyline = RoadManager.buildRoadOverlay(roads[i]);
            mRoadOverlays[i] = roadPolyline;
            if (mWhichRouteProvider == GRAPHHOPPER_BICYCLE || mWhichRouteProvider == GRAPHHOPPER_PEDESTRIAN) {
                Paint p = roadPolyline.getPaint();
                p.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));
            }
            String routeDesc = roads[i].getLengthDurationText(this, -1);
            roadPolyline.setTitle(getString(R.string.route) + " - " + routeDesc);
            roadPolyline.setInfoWindow(new BasicInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, map));
            roadPolyline.setRelatedObject(i);
            roadPolyline.setOnClickListener(new RoadOnClickListener());
            mapOverlays.add(1, roadPolyline);
            //we insert the road overlays at the "bottom", just above the MapEventsOverlay,
            //to avoid covering the other overlays.
        }
        selectRoad(0);
    }
    class RoadOnClickListener implements Polyline.OnClickListener{
        @Override public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos){
            int selectedRoad = (Integer)polyline.getRelatedObject();
            selectRoad(selectedRoad);
            polyline.showInfoWindow(eventPos);
            return true;
        }
    }

    void selectRoad(int roadIndex){
        mSelectedRoad = roadIndex;
      //  putRoadNodes(mRoads[roadIndex]);
        //Set route info in the text view:
//        TextView textView = (TextView)findViewById(R.id.routeInfo);
//        textView.setText(mRoads[roadIndex].getLengthDurationText(this, -1));
        for (int i=0; i<mRoadOverlays.length; i++){
            Paint p = mRoadOverlays[i].getPaint();
            if (i == roadIndex)
                p.setColor(0x800000FF); //blue
            else
                p.setColor(0x90666666); //grey
        }
        map.invalidate();
    }
//    private void putRoadNodes(Road road){
//        mRoadNodeMarkers.getItems().clear();
//        Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_node, null);
//        int n = road.mNodes.size();
//        MarkerInfoWindow infoWindow = new MarkerInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, map);
//        TypedArray iconIds = getResources().obtainTypedArray(R.array.direction_icons);
//        for (int i=0; i<n; i++){
//            RoadNode node = road.mNodes.get(i);
//            String instructions = (node.mInstructions==null ? "" : node.mInstructions);
//            Marker nodeMarker = new Marker(map);
//            nodeMarker.setTitle(getString(R.string.step)+ " " + (i+1));
//            nodeMarker.setSnippet(instructions);
//            nodeMarker.setSubDescription(Road.getLengthDurationText(this, node.mLength, node.mDuration));
//            nodeMarker.setPosition(node.mLocation);
//            nodeMarker.setIcon(icon);
//            nodeMarker.setInfoWindow(infoWindow); //use a shared infowindow.
//            int iconId = iconIds.getResourceId(node.mManeuverType, R.drawable.ic_empty);
//            if (iconId != R.drawable.ic_empty){
//                Drawable image = ResourcesCompat.getDrawable(getResources(), iconId, null);
//                nodeMarker.setImage(image);
//            }
//            mRoadNodeMarkers.add(nodeMarker);
//        }
//        iconIds.recycle();
//    }


    private class UpdateRoadTask extends AsyncTask<Object, Void, Road[]> {

        private final Context mContext;

        public UpdateRoadTask(Context context) {
            this.mContext = context;
        }

        protected Road[] doInBackground(Object... params) {
            @SuppressWarnings("unchecked")
            ArrayList<GeoPoint> waypoints = (ArrayList<GeoPoint>)params[0];
            RoadManager roadManager;
            Locale locale = Locale.getDefault();
            switch (mWhichRouteProvider){
                case OSRM:
                    roadManager = new OSRMRoadManager(mContext);
                    break;
                case GRAPHHOPPER_FASTEST:
                    roadManager = new GraphHopperRoadManager(graphHopperApiKey, false);
                    roadManager.addRequestOption("locale="+locale.getLanguage());
                    break;
                case GRAPHHOPPER_BICYCLE:
                    roadManager = new GraphHopperRoadManager(graphHopperApiKey, false);
                    roadManager.addRequestOption("locale="+locale.getLanguage());
                    roadManager.addRequestOption("vehicle=bike");
                    //((GraphHopperRoadManager)roadManager).setElevation(true);
                    break;
                case GRAPHHOPPER_PEDESTRIAN:
                    roadManager = new GraphHopperRoadManager(graphHopperApiKey, false);
                    roadManager.addRequestOption("locale="+locale.getLanguage());
                    roadManager.addRequestOption("vehicle=foot");
                    //((GraphHopperRoadManager)roadManager).setElevation(true);
                    break;
                case GOOGLE_FASTEST:
                    roadManager = new GoogleRoadManager();
                    break;
                default:
                    return null;
            }
            return roadManager.getRoads(waypoints);
        }

        protected void onPostExecute(Road[] result) {
            mRoads = result;
            updateUIWithRoads(result);
           // getPOIAsync(poiTagText.getText().toString());
        }
    }


    private class ReverseGeocodingTask extends AsyncTask<Object, Void, String> {
        Marker marker;
        protected String doInBackground(Object... params) {
            marker = (Marker)params[0];
            return getAddress(marker.getPosition());
        }
        protected void onPostExecute(String result) {
            marker.setSnippet(result);
            marker.showInfoWindow();
        }
    }
    public String getAddress(GeoPoint p){
        GeocoderMapzen geocoder = new GeocoderMapzen(mapzenApiKey);
        String theAddress;
        try {
            double dLatitude = p.getLatitude();
            double dLongitude = p.getLongitude();
            List<Address> addresses = geocoder.getFromLocation(dLatitude, dLongitude, 1);
            StringBuilder sb = new StringBuilder();
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                int n = address.getMaxAddressLineIndex();
                for (int i=0; i<=n; i++) {
                    if (i!=0)
                        sb.append(", ");
                    sb.append(address.getAddressLine(i));
                }
                theAddress = sb.toString();
            } else {
                theAddress = null;
            }
        } catch (IOException e) {
            theAddress = null;
        }
        if (theAddress != null) {
            return theAddress;
        } else {
            return "";
        }
    }

    public Marker updateItineraryMarker(Marker marker, GeoPoint p, int index,
                                        int titleResId, int markerResId, int imageResId, String address) {
        Drawable icon = ResourcesCompat.getDrawable(getResources(), markerResId, null);
        String title = getResources().getString(titleResId);
        if (marker == null){
            marker = new Marker(map);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
          //  marker.setInfoWindow(mViaPointInfoWindow);
            marker.setDraggable(true);
            marker.setOnMarkerDragListener(mItineraryListener);
            mItineraryMarkers.add(marker);
        }
        marker.setTitle(title+"");
        marker.setPosition(p);
        marker.setIcon(icon);
        if (imageResId != -1)
            marker.setImage(ResourcesCompat.getDrawable(getResources(), imageResId, null));
        marker.setRelatedObject(index);
        map.invalidate();
        if (address != null)
            marker.setSnippet(address);
        else
            //Start geocoding task to get the address and update the Marker description:
            new ReverseGeocodingTask().execute(marker);
        return marker;
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Search Nearby");
        View dialog= LayoutInflater.from(this).inflate(R.layout.search_dialog,null);
        builder.setView(dialog);
        final Dialog dialog1=builder.create();
        final AutoCompleteTextView completeTextView=getAutoCompleteTextViewWithData(dialog);
        Button btnSearch=dialog.findViewById(R.id.btnSearchDialog);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog1.dismiss();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(completeTextView.getWindowToken(), 0);
                //Start search:
                String feature = completeTextView.getText().toString();
                if (!feature.equals(""))
                    Toast.makeText(v.getContext(), "Searching:\n"+feature, Toast.LENGTH_LONG).show();
                getPOIAsync(feature);
            }
        });
        dialog1.show();
    }

    void getPOIAsync(String tag){
        mPoiMarkers.getItems().clear();
        new POILoadingTask().execute(tag);
    }




    private AutoCompleteTextView getAutoCompleteTextViewWithData(View dialog){
        AutoCompleteTextView completeTextView=dialog.findViewById(R.id.editPoi);
        String[] sugg=getResources().getStringArray(R.array.poi_tags);
        ArrayAdapter<String> poiAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, sugg);
        completeTextView.setAdapter(poiAdapter);
        return completeTextView;
    }

    private void showLayerOptions() {
        String[] layerOptions={"OSM Standard","OSM By Night","MapBox Satellight"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this)
        .setTitle("Layers")
        .setSingleChoiceItems(layerOptions, checked, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checked=which;
                switch (which){
                    case 0:
                        setStdTileProvider();
                        map.setTileSource(TileSourceFactory.MAPNIK);
                        map.getOverlayManager().getTilesOverlay().setColorFilter(null);
                        break;
                    case 1:
                        setStdTileProvider();
                        map.setTileSource(TileSourceFactory.MAPNIK);
                        map.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
                        break;
                    case 2:
                        setStdTileProvider();
                        map.setTileSource(MAPBOXSATELLITELABELLED);
                        map.getOverlayManager().getTilesOverlay().setColorFilter(null);
                        break;
                }
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    void setStdTileProvider(){
        if (!(map.getTileProvider() instanceof MapTileProviderBasic)){
            MapTileProviderBasic bitmapProvider = new MapTileProviderBasic(this);
            map.setTileProvider(bitmapProvider);
        }
    }

    private class POILoadingTask extends AsyncTask<Object, Void, ArrayList<POI>> {
        String mFeatureTag;
        String message;
        protected ArrayList<POI> doInBackground(Object... params) {
            mFeatureTag = (String)params[0];
            BoundingBox bb = map.getBoundingBox();
            if (mFeatureTag == null || mFeatureTag.equals("")){
                return null;
            } else if (mFeatureTag.equals("wikipedia")){
                GeoNamesPOIProvider poiProvider = new GeoNamesPOIProvider(geonamesAccount);
                //Get POI inside the bounding box of the current map view:
                ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
                return pois;
            } else if (mFeatureTag.equals("flickr")){
                FlickrPOIProvider poiProvider = new FlickrPOIProvider(flickrApiKey);
                ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
                return pois;
            } else if (mFeatureTag.startsWith("picasa")){
                PicasaPOIProvider poiProvider = new PicasaPOIProvider(null);
                //allow to search for keywords among picasa photos:
                String q = mFeatureTag.substring("picasa".length());
                ArrayList<POI> pois = poiProvider.getPOIInside(bb, 50, q);
                return pois;
            } else {
				/*
				NominatimPOIProvider poiProvider = new NominatimPOIProvider();
				ArrayList<POI> pois;
				if (mRoad == null){
					pois = poiProvider.getPOIInside(map.getBoundingBox(), mFeatureTag, 100);
				} else {
					pois = poiProvider.getPOIAlong(mRoad.getRouteLow(), mFeatureTag, 100, 2.0);
				}
				*/
                OverpassAPIProvider overpassProvider = new OverpassAPIProvider();
                String osmTag = getOSMTag(mFeatureTag);
                if (osmTag == null){
                    message = mFeatureTag + " is not a valid feature.";
                    return null;
                }
                String oUrl = overpassProvider.urlForPOISearch(osmTag, bb, 100, 10);
                ArrayList<POI> pois = overpassProvider.getPOIsFromUrl(oUrl);
                return pois;
            }
        }
        protected void onPostExecute(ArrayList<POI> pois) {
            mPOIs = pois;
            if (mFeatureTag == null || mFeatureTag.equals("")){
                //no search, no message
            } else if (mPOIs == null){
                if (message != null)
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), "Technical issue when getting "+mFeatureTag+ " POI.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), mFeatureTag+ " found:"+mPOIs.size(), Toast.LENGTH_LONG).show();
            }
            updateUIWithPOI(mPOIs, mFeatureTag);
            if (mFeatureTag.equals("flickr")||mFeatureTag.startsWith("picasa")||mFeatureTag.equals("wikipedia"))
                startAsyncThumbnailsLoading(mPOIs);
        }
    }

    void updateUIWithPOI(ArrayList<POI> pois, String featureTag){
        if (pois != null){
            POIInfoWindow poiInfoWindow = new POIInfoWindow(map);
            for (POI poi:pois){
                Marker poiMarker = new Marker(map);
                poiMarker.setTitle(poi.mType);
                poiMarker.setSnippet(poi.mDescription);
                poiMarker.setPosition(poi.mLocation);
                Drawable icon = null;
                if (poi.mServiceId == POI.POI_SERVICE_NOMINATIM || poi.mServiceId == POI.POI_SERVICE_OVERPASS_API){
                    icon = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_poi, null);
                    poiMarker.setAnchor(Marker.ANCHOR_CENTER, 1.0f);
                } else if (poi.mServiceId == POI.POI_SERVICE_GEONAMES_WIKIPEDIA){
                    if (poi.mRank < 90)
                        icon = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_poi_wikipedia_16, null);
                    else
                        icon = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_poi_wikipedia_32, null);
                } else if (poi.mServiceId == POI.POI_SERVICE_FLICKR){
                    icon = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_poi_flickr, null);
                } else if (poi.mServiceId == POI.POI_SERVICE_PICASA){
                    icon = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_poi_picasa_24, null);
                    poiMarker.setSubDescription(poi.mCategory);
                }
                poiMarker.setIcon(icon);
                poiMarker.setRelatedObject(poi);
                poiMarker.setInfoWindow(poiInfoWindow);
                //thumbnail loading moved in async task for better performances.
                mPoiMarkers.add(poiMarker);
            }
        }
        mPoiMarkers.setName(featureTag);
        mPoiMarkers.invalidate();
        map.invalidate();
    }
    String getOSMTag(String humanReadableFeature){
        HashMap<String,String> map = BonusPackHelper.parseStringMapResource(getApplicationContext(), R.array.osm_poi_tags);
        return map.get(humanReadableFeature.toLowerCase(Locale.getDefault()));
    }
    ExecutorService mThreadPool = Executors.newFixedThreadPool(3);
    class ThumbnailLoaderTask implements Runnable {
        POI mPoi; Marker mMarker;
        ThumbnailLoaderTask(POI poi, Marker marker){
            mPoi = poi; mMarker = marker;
        }
        @Override public void run(){
            Bitmap thumbnail = mPoi.getThumbnail();
            if (thumbnail != null){
                setMarkerIconAsPhoto(mMarker, thumbnail);
            }
        }
    }
    void setMarkerIconAsPhoto(Marker marker, Bitmap thumbnail){
        int borderSize = 2;
        thumbnail = Bitmap.createScaledBitmap(thumbnail, 48, 48, true);
        Bitmap withBorder = Bitmap.createBitmap(thumbnail.getWidth() + borderSize * 2, thumbnail.getHeight() + borderSize * 2, thumbnail.getConfig());
        Canvas canvas = new Canvas(withBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(thumbnail, borderSize, borderSize, null);
        BitmapDrawable icon = new BitmapDrawable(getResources(), withBorder);
        marker.setIcon(icon);
    }

    void startAsyncThumbnailsLoading(ArrayList<POI> pois){
        if (pois == null)
            return;
        //Try to stop existing threads:
        mThreadPool.shutdownNow();
        mThreadPool = Executors.newFixedThreadPool(3);
        for (int i=0; i<pois.size(); i++){
            final POI poi = pois.get(i);
            final Marker marker = mPoiMarkers.getItem(i);
            mThreadPool.submit(new ThumbnailLoaderTask(poi, marker));
        }
    }

    private void requestMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }
}
