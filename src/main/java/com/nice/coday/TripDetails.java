package com.nice.coday;

public class TripDetails {
    private int id;
    private String vehicleType;
    private double remainingBatteryPercentage;
    private String entryPoint;
    private String exitPoint;

    public TripDetails(int id, String vehicleType, double remainingBatteryPercentage, String entryPoint, String exitPoint) {
        this.id = id;
        this.vehicleType = vehicleType;
        this.remainingBatteryPercentage = remainingBatteryPercentage;
        this.entryPoint = entryPoint;
        this.exitPoint = exitPoint;
    }
    
    public int getId() {
        return id;
    }
    public String getVehicleType() {
        return vehicleType;
    }
    public double getRemainingBatteryPercentage() {
        return remainingBatteryPercentage;
    }
    public String getEntryPoint() {
        return entryPoint;
    }
    public String getExitPoint() {
        return exitPoint;
    }
}