package com.nice.coday;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import java.io.IOException;
import java.text.DecimalFormat;
import java.math.*;
import java.util.*;

@SuppressWarnings("unused")
public class ElectricityConsumptionCalculatorImpl implements ElectricityConsumptionCalculator {

    public Map<String, Double> chargingStationMap = new HashMap<>();
    public Map<String, Map<String, Long>> timeToChargeMap = new HashMap<>();
    public Map<String, Integer> vehicletrips = new HashMap<>();
    public Map<String, Double> comsumpperstation = new HashMap<>();
    public Map<String, BigDecimal> totalUnitsConsumedByVehicleType = new HashMap<>();

    
    public String nearChargingStation(Double maxDistance) {
        // System.out.println("Entered in charging Station search");
        String furthestStation = null;
        Double furthestDistance = -1.0; 
    
        for (Map.Entry<String, Double> entry : chargingStationMap.entrySet()) {
            Double stationDistance = (double)entry.getValue();
    
            if (stationDistance <= maxDistance && stationDistance > furthestDistance) {
                furthestDistance = stationDistance;
                furthestStation = entry.getKey();
                // System.out.print("This is furthest station" + furthestStation);
            }
        }
        return furthestStation;
    }

    
    public List<Double> unitsConsumed(Double start, Double exit, String vehicleType, Double iniBattery, Double maxCapacity, Double mileage) {
        Double totalDistance = exit - start;
        BigDecimal totalUnitsConsumed = BigDecimal.ZERO;
        BigDecimal totalTime = BigDecimal.ZERO;
        BigDecimal currentBattery = BigDecimal.valueOf(iniBattery).multiply(BigDecimal.valueOf(maxCapacity)).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal currentPosition = BigDecimal.valueOf(start);
        boolean tripCompleted = false;

        Map<String, Long> chargingStationTime = timeToChargeMap.get(vehicleType);

        while (currentPosition.compareTo(BigDecimal.valueOf(exit)) < 0) {
            BigDecimal remainingDistance = BigDecimal.valueOf(exit).subtract(currentPosition);
            BigDecimal possibleDistance = currentBattery.multiply(BigDecimal.valueOf(mileage)).divide(BigDecimal.valueOf(maxCapacity), 10, RoundingMode.HALF_UP);


            if (possibleDistance.compareTo(remainingDistance) >= 0) {
                // We can reach the destination
                BigDecimal unitsNeeded = remainingDistance.multiply(BigDecimal.valueOf(maxCapacity)).divide(BigDecimal.valueOf(mileage), 10, RoundingMode.HALF_UP);
                BigDecimal unitsConsumed = unitsNeeded.min(currentBattery);
                totalUnitsConsumed = totalUnitsConsumed.add(unitsConsumed);
                currentPosition = BigDecimal.valueOf(exit);
                tripCompleted = true;

            } else {
                // Find the next charging station
                String nextStation = nearChargingStation(currentPosition.add(possibleDistance).doubleValue());
                if (nextStation == null) {

                    totalUnitsConsumed = totalUnitsConsumed.add(currentBattery);
                    break;
                }

                Double stationDistance = chargingStationMap.get(nextStation);
                if (stationDistance == null || BigDecimal.valueOf(stationDistance).compareTo(currentPosition) <= 0) {
                    break;
                }

                BigDecimal distanceToStation = BigDecimal.valueOf(stationDistance).subtract(currentPosition);
                BigDecimal unitsToStation = distanceToStation.multiply(BigDecimal.valueOf(maxCapacity)).divide(BigDecimal.valueOf(mileage), 10, RoundingMode.HALF_UP);
                BigDecimal unitsConsumed = unitsToStation.min(currentBattery);
                totalUnitsConsumed = totalUnitsConsumed.add(unitsConsumed);


                Long timeReqPerUnit = chargingStationTime.get(nextStation);
                if (timeReqPerUnit == null) {

                    break;
                }

                BigDecimal unitsToCharge = BigDecimal.valueOf(maxCapacity).subtract(currentBattery.subtract(unitsConsumed));
                BigDecimal chargingTime = unitsToCharge.multiply(BigDecimal.valueOf(timeReqPerUnit));
                totalTime = totalTime.add(chargingTime);

       

                comsumpperstation.merge(nextStation, chargingTime.doubleValue(), Double::sum);

                currentBattery = BigDecimal.valueOf(maxCapacity);
                currentPosition = BigDecimal.valueOf(stationDistance);
            }
        }

        if (tripCompleted) {
            vehicletrips.merge(vehicleType, 1, Integer::sum);
            totalUnitsConsumedByVehicleType.merge(vehicleType, totalUnitsConsumed, BigDecimal::add);
            System.out.println("Trip completed successfully.");
        } else {
            System.out.println("Trip not completed.");
        }

        // System.out.println("Total units consumed: " + totalUnitsConsumed.setScale(2, RoundingMode.HALF_UP));
        // System.out.println("Total charging time: " + totalTime.setScale(2, RoundingMode.HALF_UP));

        return Arrays.asList(totalUnitsConsumed.setScale(2, RoundingMode.HALF_UP).doubleValue(),
                             totalTime.setScale(2, RoundingMode.HALF_UP).doubleValue());
    }

    //MAIN OVERIDE
    @Override
    public ConsumptionResult calculateElectricityAndTimeConsumption(ResourceInfo resourceInfo) throws IOException {


        List<ChargingStation> chargingStations = CSVReader.readChargingStationInfo(resourceInfo.getChargingStationInfoPath());

        for(ChargingStation b: chargingStations){
            String chargingstation = b.getName();
            Double distance = (double)b.getDistanceFromStart();
            chargingStationMap.put(chargingstation, distance);
        }
        List<EntryExitPoint> entryExitPoints = CSVReader.readEntryExitPointInfo(resourceInfo.getEntryExitPointInfoPath());
      
        Map<String, Double> entryexitmap = new HashMap<>();
        for(EntryExitPoint point:entryExitPoints){
            entryexitmap.put(point.getName(), (double)point.getDistanceFromStart());
        }



        Map<String, VehicleType> vehtypemap = new HashMap<>();
        List<VehicleType> vehicleTypes = CSVReader.readVehicleTypeInfo(resourceInfo.getVehicleTypeInfoPath());
        
        for(VehicleType vehicleInfo: vehicleTypes){
            vehtypemap.put(vehicleInfo.getType(),vehicleInfo);
        }

      
        timeToChargeMap = CSVReader.readTimeToChargeVehicleInfo(resourceInfo.getTimeToChargeVehicleInfoPath());
 
        List<TripDetails> tripDetailsList = CSVReader.readTripDetails(resourceInfo.getTripDetailsPath());
 
        
        Map<String, Double> unitConsumptionmap = new HashMap<>();
        Map<String, Double> timeConsumptionmap = new HashMap<>();
       
        
        for(TripDetails a:tripDetailsList){
            // System.out.println("Hrllo");

            Double start = entryexitmap.get(a.getEntryPoint());
            // System.out.println(start);

            Double end = entryexitmap.get(a.getExitPoint());
            // System.out.println(end);

            String vehtype = a.getVehicleType();
            // System.out.println(vehtype);

            Double remainingbattery = a.getRemainingBatteryPercentage();
            // System.out.println(remainingbattery);

            VehicleType vehinfo = vehtypemap.get(vehtype);

            Double unitsforfullcharge = (double)vehinfo.getUnitsForFullCharge();
            // System.out.println(unitsforfullcharge);

            Double vmileage = vehinfo.getMileage();
            // System.out.println(vmileage);
            List<Double> ans2 = unitsConsumed(start, end, vehtype, remainingbattery, unitsforfullcharge, vmileage);
            // for(Double n : ans2){
            //     System.out.println(n);
            // }
            Double initialunitconsumption = unitConsumptionmap.get(vehtype);
            if(initialunitconsumption == null){
                unitConsumptionmap.put(vehtype, ans2.get(0));
            }
            else{
                unitConsumptionmap.put(vehtype, initialunitconsumption+ans2.get(0));
            }
        
            System.out.println(ans2.get(0));
            
            Double initialtimeconsumption = timeConsumptionmap.get(vehtype);
            if(initialtimeconsumption == null){
                timeConsumptionmap.put(vehtype, ans2.get(1));
            }
            else {
                timeConsumptionmap.put(vehtype, initialtimeconsumption+ans2.get(1));
            }
            System.out.println(ans2.get(1));
        }
        
        
        // for(Map.Entry<String, Double> entry:unitConsumptionmap.entrySet()){
        //     String vehicle = entry.getKey();
        //     Double unit = entry.getValue().doubleValue();
        //     System.out.println("Vehicle: "+vehicle+" "+"Units Consumption: "+unit);
        // }

        // for(Map.Entry<String, Double> entry:timeConsumptionmap.entrySet()){
        //     String vehicle = entry.getKey();
        //     Double time = entry.getValue().doubleValue();
        //     System.out.println("Vehicle: "+vehicle+" "+"Time Required: "+time);

        // }
        // Integer totaltrips = 0;
        // for(Map.Entry<String, Integer> entry : vehicletrips.entrySet()){
        //     String vehicle = entry.getKey();
        //     Integer trips = entry.getValue();
        //     totaltrips = totaltrips + trips;
        //     System.out.println("Vehicle: "+vehicle+" "+"Trips Done: "+ trips);
        // }
        // System.out.println("Total Trips Done: "+totaltrips);

        // for(Map.Entry<String, Double> entry: comsumpperstation.entrySet()){
        //     String station = entry.getKey();
        //     Double time = entry.getValue().doubleValue();
        //     System.out.println("Station: "+ station + " "+ "Consumption Time: "+ time);
        // }


        ConsumptionResult result = new ConsumptionResult();


        List<ConsumptionDetails> detailsList = new ArrayList<>();
        

        for (Map.Entry<String, Double> entry : unitConsumptionmap.entrySet()) {
            String vehicle = entry.getKey();
            Double unit = entry.getValue();
            Double time = timeConsumptionmap.getOrDefault(vehicle, 0.0);
            Integer trips = vehicletrips.getOrDefault(vehicle, 0);


            ConsumptionDetails details = new ConsumptionDetails();
            details.setVehicleType(vehicle);
            details.setTotalUnitConsumed(unit);
            details.setTotalTimeRequired(time.longValue());
            details.setNumberOfTripsFinished(trips.longValue());

    
            detailsList.add(details);
        }

        result.setConsumptionDetails(detailsList);


        Map<String, Long> chargingStationTimeMap = new HashMap<>();
        for (Map.Entry<String, Double> entry : comsumpperstation.entrySet()) {
            String station = entry.getKey();
            Double time = entry.getValue();
            chargingStationTimeMap.put(station, time.longValue());
        }


        result.setTotalChargingStationTime(chargingStationTimeMap);

        for (ConsumptionDetails detail : result.getConsumptionDetails()) {
            System.out.println("Vehicle Type: " + detail.getVehicleType());
            System.out.println("Total Unit Consumed: " + detail.getTotalUnitConsumed());
            System.out.println("Total Time Required: " + detail.getTotalTimeRequired());
            System.out.println("Number of Trips Finished: " + detail.getNumberOfTripsFinished());
            System.out.println();
        }

        System.out.println("Total Charging Station Time:");
        for (Map.Entry<String, Long> entry : result.getTotalChargingStationTime().entrySet()) {
            String station = entry.getKey();
            Long time = entry.getValue();
            System.out.println("Station: " + station + " | Consumption Time: " + time);
        }

        return result;
    }
}



