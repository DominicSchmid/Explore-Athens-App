package com.ea.exploreathens.code;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.ea.exploreathens.R;
import com.google.android.gms.maps.model.LatLng;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CodeUtility {

    public static DateTimeFormatter dF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static DateTimeFormatter tF = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static DateTimeFormatter dtF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static String NOTIFICATION_CHANNEL;

    public static boolean firstStart = true;
    public static LatLng MAP_ZOOM_CENTER;
    public static double curlat = 0;
    public static double curlng = 0;

    private static ArrayList<Site> sites = new ArrayList<>();
    //public static String baseURL = "http://192.168.43.50:5000";
    //public static String baseURL = "http://192.168.1.18:5000";
  // public static String baseURL = "http://10.171.152.230:5000";

    public static String baseURL = "http://185.5.199.33:5053";
    public static String basePostURL = "http://185.5.199.33:5051";

    public static String[] permissions = {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};

    private CodeUtility(){
        // Private constructor
    }

    public static String getLocale(Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Log.d("preference", "Locale found: " +  prefs.getString("language", "de"));
        return prefs.getString("language", "de");
    }

    public static synchronized void setSites(ArrayList<Site> sites){
        CodeUtility.sites = sites;
    }

    public static synchronized ArrayList<Site> getSites(){
        return CodeUtility.sites;
    }

    public static String getAndroidId(Context context){
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static boolean internetAvailable(Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Popup if you really want to send your location

    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadius = 6371; // In kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = haversin(dLat) + Math.cos(lat1) * Math.cos(lon1) * haversin(dLon);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c * 1000;
    }

    private static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

    public static AlertDialog buildError(Context context, String message) {
        if(context == null)
            return null;

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Dialog_Alert);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK",(DialogInterface dialog, int id) ->{
                    // ONCLICK do things
                });
        return builder.create();
    }

    //Domme's methods
    public static double parseDouble(Object json) {
        double number = 0;
        try {
            number = (double) json;
        } catch(Exception e) {
            try {
                Long l = new Long((long) json);
                number = l.doubleValue();
            } catch(Exception f) {
                return 0;
            }
        }
        return number;
    }

    public static int parseInt(Object json) {
        int number = 0;
        try {
            number = (int) json;
        } catch(Exception e) {
            try {
                Long l = new Long((long) json);
                number = l.intValue();
            } catch(Exception f) {
                return 0;
            }
        }
        return number;
    }

    public static Object getJSON(String contentType, String responseContent){
        JSONParser parser = new JSONParser();

        try {
            // Depending on response-type return correct object
            if (contentType.equalsIgnoreCase("sites")) {
                JSONArray ja = (JSONArray) parser.parse(responseContent);

                ArrayList<Site> list = Site.parse(ja);
                System.out.println(list.toString());
                return list;
            } else if (contentType.equalsIgnoreCase("weatherforecast")) {
                JSONObject jo = (JSONObject) parser.parse(responseContent);

                WeatherForecast f = WeatherForecast.parse(jo);
                System.out.println(f.toString());
                return f;
            } else if (contentType.equalsIgnoreCase("route")) {
                JSONObject jo = (JSONObject) parser.parse(responseContent);

                Route r = Route.parse(jo);
                return r;
            }
        } catch (Exception ex) {
            String err = (ex.getMessage() == null) ? "SD Card failed" : ex.getMessage();
            ex.printStackTrace();
            Log.e("json-parse-error:", "Could not parse JSON response. Error: "+err);
        }

        return responseContent;
    }

    public static Site getSiteByName(String siteName){
        for(Site s: sites){
            if(s.getName().equalsIgnoreCase(siteName))
                return s;
        }
        return null;
    }

    public static LatLng getSiteCenter(){
        if(MAP_ZOOM_CENTER != null)
            return MAP_ZOOM_CENTER;

        double x = 0, y = 0;
        double avgX, avgY;

        if(!sites.isEmpty()){
            for(Site s : sites){
                x += s.getX();
                y += s.getY();
            }

            avgX = x / sites.size();
            avgY = y / sites.size();
            return new LatLng(avgX, avgY);
        }

        return new LatLng(37.9724132, 23.7300487);
    }

}
