package com.example.john.norfolktouring.Location;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.preference.PreferenceManager;

import com.example.john.norfolktouring.NorfolkTouring;
import com.example.john.norfolktouring.TourLocation;
import com.example.john.norfolktouring.Utils.NotificationUtils;
import com.example.john.norfolktouring.Utils.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by John on 8/27/2017.
 */

public class NearestLocationTasks {
    /*** Member Variables ***/

    /**
     * Static Member Variables
     **/

    // The application context is just a default.
    private static Context sContext = NorfolkTouring.getContext();
    // A listener for shared preferences that updates relevant fields below.
    private static SharedPreferenceListener sSharedPreferenceListener =
            new SharedPreferenceListener();
    // The source of the current location information (should be an Activity).
    private static LocationService.IReceivesLocationUpdates sLocationReceiver;
    private static Location sDeviceLocation;
    // Denotes whether notifications will be issued.
    private static boolean sNotificationsEnabled;

    // Initialize the static variables from shared preferences.
    static {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(sContext);
        sNotificationsEnabled = SharedPreferencesUtils.getNotificationsEnabled(sharedPreferences);
        sharedPreferences.registerOnSharedPreferenceChangeListener(sSharedPreferenceListener);
    }

    /**
     * Constants
     **/

    public static final String ACTION_CLOSEST_LOCATION_NOTIFICATION =
            "com.example.john.norfolktouring.action.CLOSEST_LOCATION_NOTIFICATION";
    public static final String ACTION_DISMISS_NOTIFICATION =
            "com.example.john.norfolktouring.action.DISMISS_NOTIFICATION";

    /*** Nested Classes ***/

    private static class SharedPreferenceListener
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (sContext != null) {
                if (key.equals(SharedPreferencesUtils.NOTIFICATIONS_ENABLED_KEY)) {
                    sNotificationsEnabled = SharedPreferencesUtils.getNotificationsEnabled(sharedPreferences);
                    NotificationUtils.clearAllNotifications(sContext);
                }
            }
        }
    }

    /*** Methods ***/

    /**
     * Getters and Setters
     **/

    public static void setLocationReceiver(LocationService.IReceivesLocationUpdates locationReceiver) {
        sLocationReceiver = locationReceiver;
    }

    /*** Main Methods ***/

    public static void executeTask(Context context, String action) {
        sContext = context;
        if (action != null) {
            switch (action) {
                case ACTION_CLOSEST_LOCATION_NOTIFICATION:
                    if (sNotificationsEnabled)
                        closestLocationNotification();
                    break;
                case ACTION_DISMISS_NOTIFICATION:
                    NotificationUtils.clearAllNotifications(context);
                    break;
            }
        }
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private static void closestLocationNotification() {
        // Get the device location.
        if (sLocationReceiver != null)
            sDeviceLocation = sLocationReceiver.getCurrentLocation();
        if (sDeviceLocation == null) return;
        // Get all loaded TourLocations.
        Map<String, ArrayList<TourLocation>> tourLocationsByCategory =
                TourLocation.getTourLocations();
        // Get the closest location and its associated category.
        String closestLocationName = null;
        String categoryForShortestDistance = null;
        int shortestDistance = Integer.MAX_VALUE;
        for (Map.Entry<String, ArrayList<TourLocation>> tourLocationsEntry :
                tourLocationsByCategory.entrySet()) {
            String currentCategory = tourLocationsEntry.getKey();
            ArrayList<TourLocation> tourLocations = tourLocationsEntry.getValue();
            for (TourLocation tourLocation : tourLocations) {
                Location destinationLocation = tourLocation.getLocation();
                if (destinationLocation == null) continue;
                int distance = (int) sDeviceLocation.distanceTo(destinationLocation);
                if (distance < shortestDistance) {
                    closestLocationName = tourLocation.getLocationName();
                    categoryForShortestDistance = currentCategory;
                    shortestDistance = distance;
                }
            }
        }
        // Create and show a notification (if any TourLocations have been loaded).
        if (categoryForShortestDistance != null) {
            NotificationUtils.notifyOfNearestLocation(
                    NorfolkTouring.getContext(), closestLocationName,
                    categoryForShortestDistance, shortestDistance);
        }
    }
}
