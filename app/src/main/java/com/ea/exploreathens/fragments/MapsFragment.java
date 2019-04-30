package com.ea.exploreathens.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.ea.exploreathens.MainActivity;
import com.ea.exploreathens.MySettings;
import com.ea.exploreathens.R;
import com.ea.exploreathens.RequestHelper;
import com.ea.exploreathens.SiteActivity;
import com.ea.exploreathens.code.CodeUtility;
import com.ea.exploreathens.code.Coordinate;
import com.ea.exploreathens.code.Route;
import com.ea.exploreathens.code.Site;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
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
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    private static final boolean DRAWINRADIUS = false; // TODO preferences should set this
    private OnFragmentInteractionListener mListener;

    private MapView mapView;
    private GoogleMap mMap;
    private LocationManager locationManager;
    public static String provider;
    private double currentLat = 0, currentLng = 0;
    private boolean drawPolyline;
    private List<Polyline> polylines = new ArrayList<Polyline>();

    private static final double ZOOM_ATHENS = 11;
    private static final LatLng ATHENS_LAT_LNG = new LatLng(37.9841098, 23.7213537);
    public static HashMap<String, Marker> markerList = new HashMap<>();

    private double DRAWRADIUS_KM = 2.5;

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle args = this.getArguments();
        if (args != null) {
            // Was called from Site Activity and now a route needs to be calculated
            String myValue = args.getString("routeTo");

            for (Site s : CodeUtility.getSites()) {
                if (s.getName().equals(myValue)) {
                    RouteRequest req = new RouteRequest();
                    req.execute(CodeUtility.baseURL + "/route/" + currentLat + "," + currentLng + "/" + s.getX() + "," + s.getY());
                    break;
                }
            }
        }

        new SiteRequest().execute(CodeUtility.baseURL + "/sites");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View wView = inflater.inflate(R.layout.fragment_maps, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.google_map);

        Log.d("map", "Map Fragment " + mapFragment);
        mapFragment.getMapAsync(this);



        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(provider);
        if (location != null)
            onLocationChanged(location);

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

    public void drawRadar(double meter) {
        if (mMap != null) {
            CircleOptions radarCircle;
            mMap.addCircle(radarCircle = new CircleOptions()
                    .center(ATHENS_LAT_LNG)
                    .radius(meter)
                    .strokeWidth(0f)
                    .fillColor(0x550000FF));

            //TODO: change ATHENS_LAT_LNG with position

            for (Site s : CodeUtility.getSites()) {
                if (CodeUtility.haversine(s.getX(), s.getY(), ATHENS_LAT_LNG.latitude, ATHENS_LAT_LNG.longitude) > meter) {
                    markerList.get(s.getName()).remove();
                }
            }
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

    public void drawSiteMarkers() {
        /* TODO Am Anfang sollen alle Sites angezeigt werden, die wir kennen
        TODO Durch einen Button / Menüeintrag sollen dann nur noch die Sites im gewählten Radius angezeigt werden, dann könnte man
        TODO auch die Entfernung dazuschreiben wenn das nicht zu viel Arbeit wird
         */


    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Task that tries to draw site markers on the map
        // TODO check if this breaks because clientside radiuschecking is also possible
        String url = (DRAWINRADIUS ? CodeUtility.baseURL + "/sites?radius=" + DRAWRADIUS_KM : CodeUtility.baseURL + "/sites"); // If drawinradius make radiusrequest

        if(CodeUtility.getSites().isEmpty()){
            SiteRequest req = new SiteRequest();
            req.execute(url);
        }


        //drawRadar(100.0);
        LatLng position = new LatLng(currentLat, currentLng);

        CameraPosition athens = new CameraPosition.Builder().target(ATHENS_LAT_LNG).zoom((float) ZOOM_ATHENS).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(athens));
        mMap.setMyLocationEnabled(true);

        drawMarkers();
    }

    public void drawMarkers(){
        mMap.clear(); // Clear old markers
        markerList.clear();

        for(Site s : CodeUtility.getSites()){
            Log.d("info", "Iterating item " + s.getName());
            if (mMap != null) {
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

                View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);
                TextView tvTitle = v.findViewById(R.id.tvName); // TODO change these IDS
                TextView tvDistance = v.findViewById(R.id.tvDistance);

                // TODO change this to distance or just write address?
                tvDistance.setText(site.getAddress());
                tvTitle.setText(site.getName());

                return v;
            }
        });
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


        //TODO: Make request , get destination Coordinates
        if (drawPolyline){
            LatLng destination = new LatLng(0,0);

            for(Polyline line : polylines)
                line.remove();

            polylines.clear();

            //If you arrive to the destination
            if(destination.longitude + 0.01 >= location.getLongitude() && destination.longitude - 0.01 <= location.getLongitude() &&
                    destination.latitude + 0.01 >= location.getLatitude() && destination.latitude - 0.01 <= location.getLatitude()){
                //msg: you arrived to your destination
            }else{
                redrawRoute();
                // TODO ???? drawRoute();
            }
        }
    }








    //Just "must have" methods
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public void drawRoute(Route route){
        ArrayList <Coordinate> coordinatesList = route.getCoordinates();
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

    public void redrawRoute(){
        LatLng destination = new LatLng(0,0); // TODO you need site here

        for(Polyline line : polylines)
            line.remove();

        polylines.clear();

        //If you arrive to the destination
        if(destination.longitude + 0.01 >= currentLng && destination.longitude - 0.01 <= currentLng &&
                destination.latitude + 0.01 >= currentLat && destination.latitude - 0.01 <= currentLat){
            //msg: you arrived to your destination
        }else{
            drawRoute(null); // TODO param site
            // TODO ???? drawRoute();
        }

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
        // TODO: Update argument type and name
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
                return "Oh no, an error occured :(";
            }

        }

        @Override
        protected void onPostExecute(String response){
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
                drawRoute(route);

                Log.d("json-sites", route.toString());
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
                return "Oh no, an error occured :(";
            }

        }

        @Override
        protected void onPostExecute(String response){
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

                Log.d("json-sites", sites.toString());
            } else
                Log.e("json-error", "JSON Parse error. Type not found");
        }

    }

}