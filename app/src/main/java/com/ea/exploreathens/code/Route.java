package com.ea.exploreathens.code;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class Route {

    private double distance;
    private double duration;

    private ArrayList<Step> steps = new ArrayList<>();
    private ArrayList<Coordinate> coordinates = new ArrayList<>();

    public Route(double distance, double duration, ArrayList<Step> steps, ArrayList<Coordinate> coordinates) {
        this.distance = distance;
        this.duration = duration;
        this.steps = steps;
        this.coordinates = coordinates;
    }

    @SuppressWarnings("unchecked")
    public static Route parse(JSONObject obj) {
        try {
            JSONObject features = (JSONObject)((JSONArray) obj.get("features")).get(0);
            JSONObject properties = (JSONObject) features.get("properties");
            JSONObject summary = (JSONObject) properties.get("summary");
            double summary_distance = (double) summary.get("distance");
            double summary_duration = (double) summary.get("duration");

            JSONArray waypoints = (JSONArray)((JSONObject)((JSONArray) properties.get("segments")).get(0)).get("steps");

            ArrayList<Step> steplist = new ArrayList<>();
            ArrayList<Coordinate> coordinateslist = new ArrayList<>();
            waypoints.forEach(e->{
                JSONObject content = (JSONObject) e;

                double distance = CodeUtility.parseDouble(content.get("distance"));
                double duration = CodeUtility.parseDouble(content.get("duration"));
                int type = CodeUtility.parseInt(content.get("type"));
                String instruction = (String) content.get("instruction");
                String name = (String) content.get("name");

                // TODO evtl way_points array parsen

                steplist.add(new Step(distance, duration, type, instruction, name));
            });

            JSONObject geometry = (JSONObject) features.get("geometry");
            JSONArray coords = (JSONArray) geometry.get("coordinates");
            coords.forEach(e->{
                JSONArray content = (JSONArray) e;

                double x = CodeUtility.parseDouble(content.get(0));
                double y = CodeUtility.parseDouble(content.get(1));

                coordinateslist.add(new Coordinate(x, y));
            });

            return new Route(summary_distance, summary_duration, steplist, coords);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        String toret = "Route: (Distance=" + distance + " and Duration=" + duration + "):\n";
        for(Step s : steps)
            toret += s.getInstruction() + " for duration=" + s.getDuration() + " (Distance=" + distance + ")\n";

        return toret;
    }

}

