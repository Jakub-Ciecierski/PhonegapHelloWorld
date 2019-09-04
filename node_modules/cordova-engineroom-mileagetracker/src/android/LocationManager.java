package com.engineroom.mileagetracker;

import android.util.Log;

public class LocationManager {
    private static LocationManager singletonInstance = null;

    public static LocationManager GetInstance(){
        if(singletonInstance == null){
            singletonInstance = new LocationManager();
        }
        return singletonInstance;
    }

    public void startUpdatingLocation(){
        Log.w("MileageTracker", "LocationManager::startUpdatingLocation");
    }

    public void stopUpdatingLocation(){
        Log.w("MileageTracker", "LocationManager::stopUpdatingLocation");
    }
}
