package com.ea.exploreathens;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class RequestHelper {

    public String responseContent = null;
    public String responseType = null;
    public int responseCode = 0;

    // Gets http content from request
    public String getRequestContent(String urlString){
        try{
            URL url = new URL(urlString);
            Log.d("connection", "Opening connection to " + url.toString());
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(10000); // TODO set timeout globally
            //httpURLConnection.setChunkedStreamingMode(0);
            httpURLConnection.connect();

            responseType = httpURLConnection.getHeaderField("response-type");
            responseCode = httpURLConnection.getResponseCode();

            BufferedReader rd;
            if (responseCode == 200 ||responseCode == 201)
                rd = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            else
                rd = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));

            String line;
            String content = "";
            while ((line = rd.readLine()) != null)
                content += line + "\n";

            httpURLConnection.disconnect();
            responseContent = content;
        }catch(MalformedURLException e) {
            e.printStackTrace();
            responseType = "connection-error";
            responseContent = "Server URL malformed";
        } catch (SocketTimeoutException ste){
            responseType = "connection-error";
            responseContent = "Error: Connection to server timed out!";
            Log.e("connection-error", responseContent);
        } catch (IOException e) {
            e.printStackTrace();
            responseType = "connection-error";
            responseContent = "IO exception";
        }

        return responseContent;
    }

}
