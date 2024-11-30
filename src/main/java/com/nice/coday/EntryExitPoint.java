package com.nice.coday;
public class EntryExitPoint {
    private String name;
    private double distanceFromStart;
    public EntryExitPoint(String name, double distanceFromStart) {
        this.name = name;
        this.distanceFromStart = distanceFromStart;
    }
    public String getName() {
        return name;
    }
    public double getDistanceFromStart() {
        return distanceFromStart;
    }
}
