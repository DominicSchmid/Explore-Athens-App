package com.ea.exploreathens.code;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.ea.exploreathens.PostRequest;
import com.ea.exploreathens.R;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CodeUtility {

    public static DateTimeFormatter dF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static DateTimeFormatter tF = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static DateTimeFormatter dtF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static String NOTIFICATION_CHANNEL;

    public static ArrayList<Site> sites = new ArrayList<>();
    //public static String baseURL = "http://192.168.43.50:5000";
    public static String baseURL = "http://192.168.1.18:5000";
  // public static String baseURL = "http://10.171.152.230:5000";

    public static String getAndroidId(Context context){
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static boolean internetAvailable(Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Popup if you really want to send your location
    public void showSendLocation(Context context){
        Dialog myDialog = new Dialog(context);
        myDialog.setContentView(R.layout.send_location);

        Button btnYes = myDialog.findViewById(R.id.sendLocationYesBtn);
        Button btnNo = myDialog.findViewById(R.id.sendLocationNoBtn);
        TextView aid = myDialog.findViewById(R.id.sendLocationAndroidIdTv);
        TextView text = myDialog.findViewById(R.id.sendLocationTv);

        String androidID = CodeUtility.getAndroidId(context);
        LatLng position = new LatLng(24.0, 23.1); // TODO get current position


        // TODO replace strings
        aid.setText("Android ID: " + androidID);
        text.setText("Are you sure you want to send your current location (" + position.latitude + " " + position.longitude + ")?");


        btnYes.setOnClickListener(e->{
            Log.d("info", "trying to send post request from " + androidID + " at " + position);
            PostRequest request = new PostRequest(context);
            request.execute(androidID, ""+position.latitude, ""+position.longitude);

            myDialog.dismiss();
        });

        btnNo.setOnClickListener(e->{
            myDialog.dismiss();
        });

        myDialog.show();
    }

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

    public static void showError(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK",(DialogInterface dialog, int id) ->{
                    // ONCLICK do things
                });
        AlertDialog alert = builder.create();

        try {
            alert.show();
        } catch(Exception e){
            Log.e("error", "Error showing error window " + e.getMessage());
        }
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



}
