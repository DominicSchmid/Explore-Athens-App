package com.ea.exploreathens;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.ea.exploreathens.code.CodeUtility;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostRequest extends AsyncTask< String, Void, String > {

    public int responseCode;
    public String responseType;
    public String responseContent = "";

    public Context context;

    public PostRequest(Context context){
        this.context = context;
    }

    @Override
    protected void onPostExecute(String s) {
        if(responseCode == 200 || responseCode == 201)
            Toast.makeText(context, "Successfully sent location!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "Error " + responseCode + ": Sending location failed...\n" + responseContent, Toast.LENGTH_LONG).show();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String androidID = strings[0];
            LatLng position = new LatLng(Double.parseDouble(strings[1]), Double.parseDouble(strings[2]));

            URL url = new URL(CodeUtility.basePostURL + "/addLocation/" + strings[0] + "/" + position.latitude + "/" + position.longitude);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept-Charset", "UTF-8");

            responseCode = connection.getResponseCode();
            responseContent = connection.getContentType();

            Log.d("connection", "Post connection to " + url.toString() + " opened successfully. Send location " + position.latitude + "/" + position.longitude);

            BufferedReader rd = null;
            if (connection.getResponseCode() == 200)
                rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            else
                rd = new BufferedReader(new InputStreamReader(connection.getErrorStream()));

            String line;
            while ((line = rd.readLine()) != null) {
                responseContent += line + "\n";
            }

            Log.d("json-post-content", responseContent);
        } catch (Exception e) {
            String err = (e.getMessage() == null) ? "SD Card failed" : e.getMessage();
            Log.e("connection-error", err);
            Log.e("connection-error-exception", "Exception " + e);
        }

        return responseContent;
    }
}
