package com.ea.exploreathens.code;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class Site {

    private ArrayList<String> imageRequestPaths;
    private ArrayList<String> imageLocalPaths;
    private String name;
    private String address;
    private double x;
    private double y;
    private String description;
    private double distance;

    public Site(ArrayList<String> imageRequestPaths, String name, String address, double x, double y, String description) {
        this.imageRequestPaths = imageRequestPaths;
        this.imageLocalPaths = new ArrayList<>();
        this.name = name;
        this.address = address;
        this.x = x;
        this.y = y;
        this.description = description;
        this.distance = -1;
    }

    public ArrayList<String> getImageRequestPaths() {
        return imageRequestPaths;
    }

    public String getImageLocalPath(int index){
        try {
            return imageRequestPaths.get(index);
        } catch(Exception e){
            return null;
        }
    }

    public ArrayList<String> getImageLocalPaths() {
        return imageLocalPaths;
    }

    public void addImageLocalPath(String path) {
        this.imageLocalPaths.add(path);
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getDescription() {
        return description;
    }

    public void setDistance(double distance){
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Site> parse(JSONArray obj) {
        try {
            ArrayList<Site> sites = new ArrayList<>();

            obj.forEach(e->{
                JSONObject content = (JSONObject) e;
                JSONArray imageRequestPaths = (JSONArray) content.get("images");

                ArrayList<String> requestPaths = new ArrayList<>();
                imageRequestPaths.forEach(e2->{
                    requestPaths.add(e2.toString());
                });

                String name = (String) content.get("name");
                String address = (String) content.get("address");
                double x = CodeUtility.parseDouble(content.get("x"));
                double y = CodeUtility.parseDouble(content.get("y"));
                String description = (String) content.get("description");

                double distance = 0;
                sites.add(new Site(requestPaths, name, address, x, y, description));
                try {
                    // Check if request was with xy params --> each site has a distance
                    distance = CodeUtility.parseDouble(content.get("distance"));
                    sites.get(sites.size() - 1).setDistance(distance); // Set distance for last element
                } catch(Exception omegalul) {
                    // Request was for non-distance site call

                }
            });

            return sites;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Site search(String query) {
        query = query.toLowerCase();
        if(name.toLowerCase().contains(query))
            return this;
        if(address.toLowerCase().contains(query))
            return this;
        if(description.toLowerCase().contains(query))
            return this;

        return null;
    }

    @Override
    public String toString() {
        return name + " (" + x + "/" + y + ") + (" + imageRequestPaths + "): " + description;
    }

}