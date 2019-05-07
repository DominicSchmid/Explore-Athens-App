package com.ea.exploreathens.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ea.exploreathens.MapStateManager;
import com.ea.exploreathens.MySettings;
import com.ea.exploreathens.R;
import com.ea.exploreathens.RequestHelper;
import com.ea.exploreathens.SiteActivity;
import com.ea.exploreathens.code.CodeUtility;
import com.ea.exploreathens.code.Coordinate;
import com.ea.exploreathens.code.Route;
import com.ea.exploreathens.code.Site;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private static final double ZOOM_ATHENS = 14;
    private boolean routeShowing = false;

    private LocationManager locationManager;
    public static String provider;

    public static double currentLat = 0, currentLng = 0;

    private List<Polyline> polylines = new ArrayList<>();
    public static HashMap<String, Marker> markerList = new HashMap<>();

    private SharedPreferences prefs;

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //setRetainInstance(true);

        String language = CodeUtility.getLocale(getContext());
        // Make site request if sites are not loaded yet. Just in case..
        if(CodeUtility.getSites().isEmpty())
            new SiteRequest().execute(CodeUtility.baseURL + "/sites?lan=" + language);

        Bundle args = this.getArguments();
        if (args != null) {
            // Was called from Site Activity and now a route needs to be calculated
            String myValue = args.getString("routeTo");
            Log.d("RouteTo", myValue);

            Site s = CodeUtility.getSiteByName(myValue);
            RouteRequest req = new RouteRequest();
            req.execute(CodeUtility.baseURL + "/route/" + currentLng + "," + currentLat + "/" + s.getY() + "," + s.getX());
        }

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
    }

    private void setupMapIfNeeded() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.google_map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View wView = inflater.inflate(R.layout.fragment_maps, container, false);

        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(provider);
        if (location != null)
            onLocationChanged(location);

        setupMapIfNeeded();

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return wView;
    }

    public void redrawRoute(Route route) {
        ArrayList<Coordinate> coordinatesList = route.getCoordinates();
        LatLng latLngPrevious = new LatLng(currentLat, currentLng);

        if (mMap != null) {
            for (Coordinate c : coordinatesList) {

                //get current coordinates with LatLngNext
                LatLng latLngNext = new LatLng(c.getX(), c.getY());

                polylines.add(mMap.addPolyline((new PolylineOptions())
                        .add(latLngPrevious, latLngNext)
                        .width(5)
                        .color(Color.BLUE)
                        .geodesic(true)));

                latLngPrevious = latLngNext;
            }
        }
    }


    public void drawRadar() {
        if (mMap != null) {
            double drawradius = prefs.getInt("seek_bar_radar", 500);

            Log.d("radar", "Starting radar for " + drawradius + " meters");
            mMap.addCircle(new CircleOptions()
                    .center(new LatLng(currentLat, currentLng))
                    .radius(drawradius)
                    .strokeWidth(0f)
                    .fillColor(0x550000FF));
        }

    }


    /**
     * Gets the Site object from a Marker on the Google Map
     *
     * @param arg0 The Marker
     * @return the Site object for the coordinates of the Marker
     */
    private Site getSiteFromMarker(Marker arg0) {
        LatLng latLng = arg0.getPosition();
        Site site = null;

        for (Site sit : CodeUtility.getSites()) {
            //Toast.makeText(getApplicationContext(), lat+"----"+lon+" = " + sit.getX() + "----" + sit.getY(),Toast.LENGTH_SHORT).show();
            // Site von diesem Marker gefunden
            if (sit.getX() == latLng.latitude && sit.getY() == latLng.longitude) {
                Log.d("map-siteinfo", "Clicked site " + sit.getName() + " with " + sit.getX() + "/" + sit.getY() + " coressponding to " + latLng.latitude + "/" + latLng.longitude);
                return sit;
            }
        }

        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);


        if(CodeUtility.getSites().isEmpty()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        if(CodeUtility.firstStart) {
            Log.d("info", "First start detected: Zooming into " + CodeUtility.getSiteCenter().toString());
            CameraPosition athens = new CameraPosition.Builder().target(CodeUtility.getSiteCenter()).zoom((float) ZOOM_ATHENS).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(athens));
            CodeUtility.firstStart = false;
            MapStateManager mgr = new MapStateManager(getContext());
            mgr.saveMapState(mMap);
        } else {
            MapStateManager mgr = new MapStateManager(getContext());
            CameraPosition position = mgr.getSavedCameraPosition();
            if (position != null) {
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);

                mMap.moveCamera(update);
                mMap.setMapType(mgr.getSavedMapType());
            }
        }

        drawMarkers();
    }

    public void drawMarkers(){
        mMap.clear(); // Clear old markers
        markerList.clear();
        double drawradius = prefs.getInt("seek_bar_radar", 500);
        boolean radarEnabled = prefs.getBoolean("radar_switch", false);

        if (mMap == null)
            return;

        if(radarEnabled)
            drawRadar();

        for(Site s : CodeUtility.getSites()){
            if (radarEnabled) {
                if(CodeUtility.haversine(s.getX(), s.getY(), currentLat, currentLng) + 250 < drawradius) {
                    Log.d("radar", "Distance: " + CodeUtility.haversine(s.getX(), s.getY(), currentLat, currentLng));
                    Log.d("info", "Iterating item " + s.getName());
                    LatLng position = new LatLng(s.getX(), s.getY());
                    MarkerOptions opt = new MarkerOptions()
                            .title(s.getName())
                            .position(position)
                            .snippet(s.getDescription());
                    Log.d("info", "Put Marker on map_menu " + opt.toString());
                    markerList.put(s.getName(), mMap.addMarker(opt));
                }
            } else { // Sonst alle Marker zeichnen
                Log.d("info", "Iterating item " + s.getName());
                LatLng position = new LatLng(s.getX(), s.getY());
                MarkerOptions opt = new MarkerOptions()
                        .title(s.getName())
                        .position(position)
                        .snippet(s.getDescription());
                Log.d("info", "Put Marker on map_menu " + opt.toString());
                markerList.put(s.getName(), mMap.addMarker(opt));
            }
        }

        mMap.setOnInfoWindowClickListener(new InfoClickListener());

        // This inflates the info window when you click on a Marker
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){
            @Override
            public View getInfoWindow (Marker arg0){
                return null;
            }

            @Override
            public View getInfoContents (Marker arg0){
                Site site = getSiteFromMarker(arg0);

                View v = getLayoutInflater().inflate(R.layout.maps_site_info, null);
                TextView tvTitle = v.findViewById(R.id.map_info_name_tv);
                TextView tvDistance = v.findViewById(R.id.map_info_street_tv);


                tvDistance.setText(site.getAddress());
                tvTitle.setText(site.getName());

                return v;
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        if(mMap != null) {
            MapStateManager mgr = new MapStateManager(getContext());
            mgr.saveMapState(mMap);
        }
    }

    // Resumes state of map
    @Override
    public void onResume() {
        super.onResume();
        setupMapIfNeeded();
    }


    class InfoClickListener implements GoogleMap.OnInfoWindowClickListener {

        @Override
        public void onInfoWindowClick(Marker marker) {
            Site site = getSiteFromMarker(marker);
            Log.d("maps", "Clicked on infomarker of " + site.getName());
            Intent intent = new Intent(getActivity(), SiteActivity.class);
            Bundle b = new Bundle();
            b.putInt("siteindex", CodeUtility.getSites().indexOf(site));
            intent.putExtras(b); //Put your id to your next Intent
            startActivity(intent);
        }
    }

    // A pop-up asks if the app can use the service "ACCESS_COARSE_LOCATION" and "ACCESS_FINE_LOCATION"

    //This method initializes the upper right menu corner
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    //onOptionsItemSelected sets up the action to complete if a menu option is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_normal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.action_hybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.action_satellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.action_settings:
                Intent intent = new Intent(getActivity(), MySettings.class);
                startActivity(intent);
                break;
        }
        return false;
    }

    // If the phone location is changed it will change the latitude and longitude of the variable location
    @Override
    public void onLocationChanged(Location location) {
        currentLat = location.getLatitude();
        currentLng = location.getLongitude();

        if (routeShowing){
            LatLng destination = new LatLng(0,0); // TODO Koordinaten des Routenziels holen

            for(Polyline line : polylines)
                line.remove();

            polylines.clear();

            //If you arrive to the destination
            if(destination.longitude + 0.01 >= location.getLongitude() && destination.longitude - 0.01 <= location.getLongitude() &&
                    destination.latitude + 0.01 >= location.getLatitude() && destination.latitude - 0.01 <= location.getLatitude()){
                //msg: you arrived to your destination
                routeShowing = false;

                showError("Success: You have arrived at your destination!");
                // start site activity
            }
        }
    }

    public void drawRoute(Route route){
        ArrayList <Coordinate> coordinatesList = route.getCoordinates();
        LatLng latLngPrevious = new LatLng(currentLat, currentLng);

        if (mMap != null) {
            for (Coordinate c : coordinatesList) {
                Log.d("route", "Drawing " + c.toString());

                //get current coordinates with LatLngNext
                LatLng latLngNext = new LatLng(c.getY(), c.getX());

                polylines.add(mMap.addPolyline((new PolylineOptions())
                        .add(latLngPrevious, latLngNext)
                        .width(5)
                        .color(Color.BLUE)
                        .geodesic(true)));

                latLngPrevious = latLngNext;
            }
        }
    }


    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void showError(String message){
        CodeUtility.buildError(getActivity(), message).show();
        //pullToRefresh.setRefreshing(false);
    }

    class RouteRequest extends AsyncTask<String, Void, String> {

        private String contentType;
        public Route route;

        @Override
        protected String doInBackground(String... urls) {

            String responseType = "";

            try {
                if(!CodeUtility.internetAvailable(getContext()))
                    return "Internet not available";

                RequestHelper helper = new RequestHelper();
                helper.getRequestContent(urls[0]);
                Log.d("connection-helper", "Helper " + helper.toString() + " returned ");
                contentType = helper.responseType;

                Log.d("json-response", helper.responseContent);
                Log.d("json-responsetype", helper.responseType);
                Log.d("json-responsecode", ""+helper.responseCode);

                return helper.responseContent;
            } catch (Exception e) {
                e.printStackTrace();
                //String err = (e.getMessage() == null) ? "SD Card failed" : e.getMessage();
                //og.e("connection-error", ""+err);
                return  "The operation can not be performed on the selected server (" + CodeUtility.baseURL + ")";
            }
        }

        @Override
        protected void onPostExecute(String response){
            if(getContext() == null)
                return;

            Object obj = CodeUtility.getJSON(contentType, response);

            if(obj instanceof String) {
                // Error
                showError(""+obj);
                Log.e("error", obj.toString());
                // If response is String an error occured and you can read it by calling string
            } else if (obj instanceof Route){
                // Otherwise a request to /sites must return AL<Site> so you can just cast
                Route route = (Route) obj;
                this.route = route;

                routeShowing = true;
                drawRoute(route);

                Log.d("json-route", route.toString());
            } else
                Log.e("json-error", "JSON Parse error. Type not found");
        }

    }

    class SiteRequest extends AsyncTask< String, Void, String > {

        private String contentType;

        @Override
        protected String doInBackground(String... urls) {

            String responseType = "";

            try {
                if(!CodeUtility.internetAvailable(getContext()))
                    return "Internet not available";

                RequestHelper helper = new RequestHelper();
                helper.getRequestContent(urls[0]);
                Log.d("connection-helper", "Helper " + helper.toString() + " returned ");
                contentType = helper.responseType;

                Log.d("json-response", helper.responseContent);
                Log.d("json-responsetype", helper.responseType);
                Log.d("json-responsecode", ""+helper.responseCode);

                return helper.responseContent;
            } catch (Exception e) {
                e.printStackTrace();
                //String err = (e.getMessage() == null) ? "SD Card failed" : e.getMessage();
                //og.e("connection-error", ""+err);
                return "The operation can not be performed on the selected server (" + CodeUtility.baseURL + ")";
            }

        }

        @Override
        protected void onPostExecute(String response){
            if(getContext() == null)
                return;

            Object obj = CodeUtility.getJSON(contentType, response);

            if(obj instanceof String) {
                // Error
                showError(""+obj);
                Log.e("error", obj.toString());
                // If response is String an error occured and you can read it by calling string
            } else if (obj instanceof ArrayList){
                // Otherwise a request to /sites must return AL<Site> so you can just cast
                // Sites
                ArrayList<Site> sites = (ArrayList<Site>) obj;
                CodeUtility.setSites(sites);

                setupMapIfNeeded();
                //drawMarkers();
                Log.d("json-sites", sites.toString());
            } else
                Log.e("json-error", "JSON Parse error. Type not found");
        }

    }

    //Just "must have" methods
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

}
