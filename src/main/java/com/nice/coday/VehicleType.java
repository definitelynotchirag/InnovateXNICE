package com.nice.coday;
public class VehicleType {
    private String type;
    private int unitsForFullCharge;
    private double mileage;
    public VehicleType(String type, int unitsForFullCharge, double mileage) {
        this.type = type;
        this.unitsForFullCharge = unitsForFullCharge;
        this.mileage = mileage;
    }
    public String getType() {
        return type;
    }
    public int getUnitsForFullCharge() {
        return unitsForFullCharge;
    }
    public double getMileage() {
        return mileage;
    }
}