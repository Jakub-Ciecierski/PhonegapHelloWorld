package com.engineroom.mileagetracker;

import android.util.Log;

import java.io.File;

public class TripManager {
    private static TripManager singletonInstance = null;

    public static TripManager GetInstance(){
        if(singletonInstance == null){
            singletonInstance = new TripManager();
        }
        return singletonInstance;
    }

    public void startTrip(File databasePath){
        Log.w("MileageTracker", "TripManager::startTrip");

        TripDBManager tripDBManager = TripDBManager.GetInstance();
        tripDBManager.openDB(databasePath);

        LocationManager locationManager = LocationManager.GetInstance();
        locationManager.startUpdatingLocation();

        // <>
        TripDBManager.GPSData gpsData = tripDBManager.new GPSData();
        gpsData.distanceSoFar = 1.1;
        gpsData.latitude = 2.2;
        gpsData.longitude = 3.3;
        gpsData.timestamp = 4.4;
        tripDBManager.writeDB(gpsData);
        // !<>

        Log.w("MileageTracker", "Hello::startTrip - databasePath = " + databasePath.getAbsolutePath());

        tripDBManager.readDBLatestRow();
    }

    public void pauseTrip(){
        Log.w("MileageTracker", "TripManager::pauseTrip");

        LocationManager locationManager = LocationManager.GetInstance();
        locationManager.stopUpdatingLocation();

        TripDBManager tripDBManager = TripDBManager.GetInstance();
        tripDBManager.closeDB();
    }

    public void stopTrip(){
        Log.w("MileageTracker", "TripManager::stopTrip");

        LocationManager locationManager = LocationManager.GetInstance();
        locationManager.stopUpdatingLocation();

        TripDBManager tripDBManager = TripDBManager.GetInstance();
        tripDBManager.clearDBTable();
        tripDBManager.closeDB();
    }

}
