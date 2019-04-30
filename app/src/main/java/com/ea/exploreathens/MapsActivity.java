package com.ea.exploreathens;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.ea.exploreathens.code.CodeUtility;
import com.ea.exploreathens.code.Coordinate;
import com.ea.exploreathens.code.Route;
import com.ea.exploreathens.code.Site;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
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


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private String provider;
    private double currentLat=0, currentLng=0;
    private boolean drawPolyline;
    private List<Polyline> polylines = new ArrayList<Polyline>();

    private static final double ZOOM_ATHENS = 11;
    private static final LatLng ATHENS_LAT_LNG = new LatLng(37.9841098,23.7213537);
    public static HashMap<String, Marker> markerList = new HashMap<>();
    public static final double r = 6371; // In kilometers


    /*
        In this method the map_menu gets loaded up and afterwards the program checks if there it can use the GSP.
        if not the method "getPermission()" will be called. At the end a Location class is initialized and
        checks with onLocationChanged if the phones changes.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            getPermission();

        }else{
            Location location = locationManager.getLastKnownLocation(provider);

            if(location != null){
                onLocationChanged(location);
            }
        }

        //SearchView searchStuff = (SearchView) findViewById(R.id.search_stuff);

    }

    /*
        In "onMapReady" the map_menu gets actually loaded and with CcameraPosition the initial map_menu starts in Athens (See variables ZOOM_ATHENS and ATHENS_LAT_LNG)
        At the end the it looks if the required permission are given, otherwise it will ask for them (see getPermission)
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //drawRadar(100.0);
        LatLng position = new LatLng(currentLat, currentLng);

        CameraPosition athens = new CameraPosition.Builder().target(ATHENS_LAT_LNG).zoom((float)ZOOM_ATHENS).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(athens));

        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            getPermission();
        }else {
            mMap.setMyLocationEnabled(true);
        }
    }

    public void drawMarkers(){
        //TODO drawMarker(mMap);
        drawRadar(100);
    }


    public void redrawRoute(Route route){
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

    public void drawSiteMarkers() {
        /* TODO Am Anfang sollen alle Sites angezeigt werden, die wir kennen
        TODO Durch einen Button / Menüeintrag sollen dann nur noch die Sites im gewählten Radius angezeigt werden, dann könnte man
        TODO auch die Entfernung dazuschreiben wenn das nicht zu viel Arbeit wird
         */


        for (Site s : CodeUtility.sites) {
            Log.d("info", "Iterating item " + s);
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
        mMap.setInfoWindowAdapter(new InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker arg0) {
                Site site = getSiteFromMarker(arg0);

                View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);
                TextView tvTitle = v.findViewById(R.id.tvName); // TODO change these IDS
                TextView tvDistance =  v.findViewById(R.id.tvDistance);

                // TODO change this to distance or just write address?
                tvDistance.setText(site.getAddress());
                tvTitle.setText(site.getName());

                return v;
            }
        });
    }

    public void drawRadar(double meter){
        if(mMap != null){
            CircleOptions radarCircle;
            mMap.addCircle(radarCircle = new CircleOptions()
                    .center(ATHENS_LAT_LNG)
                    .radius(meter)
                    .strokeWidth(0f)
                    .fillColor(0x550000FF));

            //TODO: change ATHENS_LAT_LNG with position

            for (Site s : CodeUtility.sites) {
                if(haversine(s.getX(), s.getY(), ATHENS_LAT_LNG.latitude, ATHENS_LAT_LNG.longitude) > meter){
                    markerList.get(s.getName()).remove();
                }
            }
        }

    }


    /**
     * Gets the Site object from a Marker on the Google Map
     * @param arg0 The Marker
     * @return the Site object for the coordinates of the Marker
     */

    private Site getSiteFromMarker(Marker arg0) {
        LatLng latLng = arg0.getPosition();
        Site site = null;

        for(Site sit : CodeUtility.sites){
            //Toast.makeText(getApplicationContext(), lat+"----"+lon+" = " + sit.getX() + "----" + sit.getY(),Toast.LENGTH_SHORT).show();
            // Site von diesem Marker gefunden
            if(sit.getX() == latLng.latitude && sit.getY() == latLng.longitude)
                return sit;
        }

        return null;
    }

    class InfoClickListener implements GoogleMap.OnInfoWindowClickListener {

        @Override
        public void onInfoWindowClick(Marker marker) {
            Site site = getSiteFromMarker(marker);
            Intent intent = new Intent(MapsActivity.this, SiteActivity.class);
            Bundle b = new Bundle();
            b.putString("sitename", site.getName());
            intent.putExtras(b); //Put your id to your next Intent
            startActivity(intent);
        }
    }

    // A pop-up asks if the app can use the service "ACCESS_COARSE_LOCATION" and "ACCESS_FINE_LOCATION"
    public void getPermission(){
        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION},
                0);
    }
    //This method initializes the upper right menu corner
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }*/

    //onOptionsItemSelected sets up the action to complete if a menu option is clicked
   /* @Override
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
                Intent intent = new Intent(MapsActivity.this, Settings.class);
                startActivity(intent);
                break;
        }
        return true;
    }*/



    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = haversin(dLat) + Math.cos(lat1) * Math.cos(lon1) * haversin(dLon);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c * 1000;
    }

    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
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
                //TODO redrawRoute();
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

}


