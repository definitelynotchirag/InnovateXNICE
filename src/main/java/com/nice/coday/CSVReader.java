package com.nice.coday;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CSVReader {
    // Read Charging Station Info
    public static List<ChargingStation> readChargingStationInfo(Path path) throws IOException {
        List<ChargingStation> chargingStations = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                ChargingStation station = new ChargingStation(values[0], Integer.parseInt(values[1]));
                chargingStations.add(station);
            }
        }
        System.out.println("Read successfully 1");
        return chargingStations;
    }

    // Read EntryExit Point Info
    public static List<EntryExitPoint> readEntryExitPointInfo(Path path) throws IOException {
        List<EntryExitPoint> entryExitPoints = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                EntryExitPoint point = new EntryExitPoint(values[0], Double.parseDouble(values[1]));
                entryExitPoints.add(point);
            }
        }
        System.out.println("Read successfully 2");
        return entryExitPoints;
    }

    // Read Vehicle Type Info
    public static List<VehicleType> readVehicleTypeInfo(Path path) throws IOException {
        List<VehicleType> vehicleTypes = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                VehicleType vehicleType = new VehicleType(values[0], Integer.parseInt(values[1]), Double.parseDouble(values[2]));
                vehicleTypes.add(vehicleType);
            }
        }
        System.out.println("Read successfully 3");
        return vehicleTypes;
    }

    // Read Time to Charge Vehicle Info
    public static Map<String, Map<String, Long>> readTimeToChargeVehicleInfo(Path path) throws IOException {
        Map<String, Map<String, Long>> timeToChargeMap = new HashMap<>();
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                String vehicleType = values[0];
                String chargingStation = values[1];
                long timeToCharge = Long.parseLong(values[2]);
                timeToChargeMap
                    .computeIfAbsent(vehicleType, k -> new HashMap<>())
                    .put(chargingStation, timeToCharge);
            }
        }
        System.out.println("Read successfully 4");
        return timeToChargeMap;
    }
    
    // Read Trip Details
    public static List<TripDetails> readTripDetails(Path path) throws IOException {
        List<TripDetails> tripDetailsList = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                TripDetails trip = new TripDetails(Integer.parseInt(values[0]), values[1], Double.parseDouble(values[2]), values[3], values[4]);
                tripDetailsList.add(trip);
            }
        }
        System.out.println("Read successfully 5");
        return tripDetailsList;
    }
}
