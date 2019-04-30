package com.ea.exploreathens.code;

public class Step {

    private double distance;
    private double duration;
    private long type;
    private String instruction;
    private String name;

    //private static ArrayList<longeger> waypolongs = new ArrayList<>();

    public Step(double distance, double duration, long type, String instruction, String name) {
        this.distance = distance;
        this.duration = duration;
        this.type = type;
        this.instruction = instruction;
        this.name = name;
    }

    public double getDistance() {
        return distance;
    }

    public double getDuration() {
        return duration;
    }

    public long getType() {
        return type;
    }

    public String getInstruction() {
        return instruction;
    }

    public String getName() {
        return name;
    }

//	public ArrayList<longeger> getWaypolongs() {
//		return waypolongs;
//	}

}

