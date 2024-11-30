package com.nice.coday;
public class ChargingStation {
    private String name;
    private Integer distanceFromStart;
    public ChargingStation(String name, Integer distanceFromStart) {
        this.name = name;
        this.distanceFromStart = distanceFromStart;
    }
    public String getName() {
        return name;
    }
    public Integer getDistanceFromStart() {
        return distanceFromStart;
    }
}