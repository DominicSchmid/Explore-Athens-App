package com.ea.exploreathens.code;

import org.json.simple.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Weather {

    private String name;
    private LocalDate date;
    private LocalTime time;
    private double min_temp;
    private double max_temp;
    private double temp;
    private int humidity;
    private String icon;
    private String description;

    private boolean fromForecast;

    public Weather(String name, LocalDate date, LocalTime time, double min_temp, double max_temp, double temp,
                   int humidity, String icon, String description) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.min_temp = min_temp;
        this.max_temp = max_temp;
        this.temp = temp;
        this.humidity = humidity;
        this.icon = icon;
        this.description = description;
        this.fromForecast = false;
    }

    public Weather(String name, LocalDateTime datetime, double min_temp, double max_temp, double temp,
                   int humidity, String icon, String description) {
        this.name = name;
        this.date = datetime.toLocalDate();
        this.time = datetime.toLocalTime();
        this.min_temp = min_temp;
        this.max_temp = max_temp;
        this.temp = temp;
        this.humidity = humidity;
        this.icon = icon;
        this.description = description;
        this.fromForecast = true;
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

    public boolean isFromForecast() {
        return fromForecast;
    }

    public double getMin_temp() {
        return min_temp;
    }

    public double getMax_temp() {
        return max_temp;
    }

    public double getTemp() {
        return temp;
    }

    public int getHumidity() {
        return humidity;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public static Weather parse(JSONObject obj) {
        try {

            //System.out.println(obj);
            String name = (String) obj.get("name");
            LocalDate date = LocalDate.parse((String) obj.get("date"), CodeUtility.dF);
            LocalTime time = LocalTime.parse((String) obj.get("time"), CodeUtility.tF);
            double min_temp = CodeUtility.parseDouble(obj.get("min_temp"));
            double max_temp = CodeUtility.parseDouble(obj.get("max_temp"));
            double temp = CodeUtility.parseDouble(obj.get("temp"));
            int humidity = CodeUtility.parseInt(obj.get("humidity"));
            String icon = (String) obj.get("icon");
            String description = (String) obj.get("description");

            return new Weather(name, date, time ,min_temp, max_temp, temp, humidity, icon, description);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return name + " (" + date + " " + time + "): Min=" + min_temp + "°C Max=" + max_temp + "°C Cur=" + temp + "°C Hum=" + humidity + " " + description;
    }

}

