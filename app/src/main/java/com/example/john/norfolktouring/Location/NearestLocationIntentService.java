package com.example.john.norfolktouring.Location;

import android.app.Activity;
import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

/**
 * Issues recurring notifications regarding the closest location.
 */
public class NearestLocationIntentService extends IntentService {
    /*** Member Variables ***/
    // The Activity that this service is currently running for.
    private Activity mClientActivity;

    /**
     * Location Updates
     **/
    private LocationService mLocationService;
    private boolean mLocationServiceBound = false;
    private boolean mReceivingLocationUpdates = false;
    private ServiceConnection mLocationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocationServiceBinder binder =
                    (LocationService.LocationServiceBinder) service;
            mLocationService = binder.getService();
            mLocationServiceBound = true;
            // Start requesting location updates if not already doing so.
            attemptStartLocationUpdates();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLocationServiceBound = false;
            mLocationService = null;
        }
    };

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    /**
     * Callback that is called when the `LocationService` service receives a location update.
     */
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            // Log that we are receiving location updates and store the location.
            mReceivingLocationUpdates = true;
            mCurrentLocation = locationResult.getLastLocation();
        }
    };

    /**
     * Constants
     **/

    public static final int CLOSEST_LOCATION_NOTIFICATION_PENDING_INTENT = 1;
    public static final int CLOSEST_LOCATION_NOTIFICATION_ID = 2;

    /*** Methods ***/

    /**
     * Main Methods
     **/

    public NearestLocationIntentService() {
        super("NearestLocationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            NearestLocationTasks.executeTask(this, action);
        }
    }

    /**
     * Location Information Methods
     **/
    // TODO: Remove this.
    private void attemptStartLocationUpdates() {
        if (mLocationService.checkPermissions())
            mLocationService.startLocationUpdates(mClientActivity, mLocationCallback);
        else
            mLocationService.requestPermissions(mClientActivity);
    }
}
