package com.ea.exploreathens.code;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

public class WeatherForecast {

    private String name;
    private LocalDate date;
    private LocalTime time;
    private ArrayList<Weather> forecast = new ArrayList<>();

    public WeatherForecast(String name, LocalDate date, LocalTime time, ArrayList<Weather> forecast) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.forecast = forecast;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public ArrayList<Weather> getForecast() {
        return forecast;
    }


    public static WeatherForecast parse(JSONObject obj) {
        try {
            String name = (String) obj.get("name");
            LocalDate date = LocalDate.parse((String) obj.get("date"), CodeUtility.dF);
            LocalTime time = LocalTime.parse((String) obj.get("time"), CodeUtility.tF);

            JSONArray weathers = (JSONArray) obj.get("forecast");

            ArrayList<Weather> forecast = new ArrayList<>();
            weathers.forEach(e->{
                JSONObject content = (JSONObject) e;

                double min_temp = CodeUtility.parseDouble(content.get("min_temp"));
                double max_temp = CodeUtility.parseDouble(content.get("max_temp"));
                double temp = CodeUtility.parseDouble(content.get("temp"));
                int humidity = CodeUtility.parseInt(content.get("humidity"));
                String icon = (String) content.get("icon");
                String description = (String) content.get("description");
                LocalDateTime datetime = LocalDateTime.parse((String) content.get("dt_txt"), CodeUtility.dtF);
                //LocalDateTime datetime = LocalDateTime.now();
                forecast.add(new Weather(name, datetime, min_temp, max_temp, temp, humidity, icon, description));
            });

            return new WeatherForecast(name, date, time, forecast);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<String> getDayNames(){
        ArrayList<String> dates = new ArrayList<>();

        for(int i = 0; i < forecast.size(); i++){
            String day = forecast.get(i).getDate().getDayOfWeek().toString();
            if(!dates.contains(day))
                dates.add(day);
        }

        return dates;
    }

    public ArrayList<ArrayList<Weather>> getForecastSplitByDays(){
        ArrayList<Weather> day = new ArrayList<>();
        ArrayList<ArrayList<Weather>> splitted = new ArrayList<>();

        if(forecast.isEmpty())
            return splitted;

        day.add(forecast.get(0));
        for(int i = 1; i < forecast.size(); i++){
            if(forecast.get(i).getDate().isEqual(forecast.get(i - 1).getDate()))
                day.add(forecast.get(i));
            else {
                splitted.add(day);
                day = new ArrayList<>();
                day.add(forecast.get(i));
            }
        }
        splitted.add(day); // Last item needs to be added too

        return splitted;
    }

    public static double getAvgTemperature(ArrayList<Weather> ws){
        int count = 0;
        int avg = 0;
        for(Weather w : ws){
            avg += w.getMin_temp() + w.getMax_temp();
            count += 2;
        }

        if(count > 0)
            return avg / count;
        return avg;
    }

    public ArrayList<Weather> getForecastForDate(LocalDate date){
        ArrayList<Weather> dates = new ArrayList<>();
        for(Weather w : forecast){
            if(w.getDate().isEqual(date))
                dates.add(w);
        }

        return dates;
    }

    @Override
    public String toString() {
        String toret = "Forecast for " + name + ":\n";
        for(Weather w : forecast)
            toret += w.getTime() + ": Min=" + w.getMin_temp() + "°C Max=" + w.getMax_temp() + "°C Cur=" + w.getTemp() + "°C Hum=" + w.getHumidity() + " " + w.getDescription() + "\n";

        return toret;
    }

}

