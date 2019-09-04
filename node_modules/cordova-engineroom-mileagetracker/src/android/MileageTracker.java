package com.engineroom.mileagetracker;

import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;

public class MileageTracker extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("greet")) {
            return greet(data, callbackContext);
        }
        else if (action.equals("startTrip")){
            return startTrip(data, callbackContext);
        }
        else if (action.equals("stopTrip")){
            return stopTrip(data, callbackContext);
        }
        else if (action.equals("pauseTrip")){
            return pauseTrip(data, callbackContext);
        }
        return false;
    }

    private boolean greet(JSONArray data, CallbackContext callbackContext)  throws JSONException{
        String name = data.getString(0);
        String message = "Hello, " + name;
        callbackContext.success(message);

        return true;
    }

    private boolean startTrip(JSONArray data, CallbackContext callbackContext)  throws JSONException{
        Log.w("MileageTracker", "Hello::startTrip");

        File databasePath = this.cordova.getActivity().getDatabasePath("TripDB.db");
        TripManager tripManager = TripManager.GetInstance();
        tripManager.startTrip(databasePath);

        String message = "startTrip callback";
        callbackContext.success(message);

        return true;
    }

    private boolean stopTrip(JSONArray data, CallbackContext callbackContext)  throws JSONException{
        Log.w("MileageTracker", "Hello::stopTrip");

        String message = "stopTrip callback";
        callbackContext.success(message);

        TripManager tripManager = TripManager.GetInstance();
        tripManager.pauseTrip();

        return true;
    }

    private boolean pauseTrip(JSONArray data, CallbackContext callbackContext)  throws JSONException{
        Log.w("MileageTracker", "Hello::pauseTrip");

        String message = "pauseTrip callback";
        callbackContext.success(message);

        TripManager tripManager = TripManager.GetInstance();
        tripManager.stopTrip();

        return true;
    }
}
