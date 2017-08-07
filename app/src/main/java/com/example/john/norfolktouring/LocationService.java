package com.example.john.norfolktouring;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Created by John on 7/1/2017.
 */

/**
 * A Service class for handling location updates (a type of singleton).
 * Much of this code was derived from
 * <a href="https://github.com/googlesamples/android-play-location/blob/master/LocationUpdates/app
 * /src/main/java/com/google/android/gms/location/sample/locationupdates/MainActivity.java">
 * this Java file</a>.
 */
public class LocationService extends Service {
    /*** Member Variables ***/

    private IBinder mBinder;

    /** Location Updates **/

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;

    /**
     * Stores parameters for requests to the Fused Location Provider API.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;

    /** Constants **/

    /**
     * Arbitrary integer constant denoting a request for access to fine location.
     */
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;

    /**
     * Constant used in the location settings dialog.
     */
    public static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private static String LOG_TAG = LocationService.class.getCanonicalName();

    /*** Methods ***/

    /**
     * Lifecycle Methods
     **/

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new LocationServiceBinder();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(NorfolkTouring.getContext());
        mSettingsClient = LocationServices.getSettingsClient(NorfolkTouring.getContext());
        createLocationRequest();
        createLocationSettingsRequest();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Main Methods
     **/

    /**
     * Binds an `Activity` to this `Service`.
     */
    public static void bind(Activity activity, ServiceConnection serviceConnection) {
        Intent locationServiceIntent = new Intent(activity, LocationService.class);
        activity.bindService(locationServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public LocationRequest createLocationRequest() {
        mLocationRequest = new LocationRequest();
        // Sets the desired interval for active location updates.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        // Sets the fastest rate for active location updates.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    public LocationSettingsRequest createLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
        return mLocationSettingsRequest;
    }

    public void startLocationUpdates(final Activity activity,
                                     final LocationCallback locationCallback) {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(LOG_TAG, "All location settings are satisfied.");

                        mLocationCallback = locationCallback;

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback,
                                Looper.myLooper());
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(LOG_TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling `startResolutionForResult()`,
                                    // and check the result in `onActivityResult()`.
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(LOG_TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(LOG_TAG, errorMessage);
                                Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                });
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    public void stopLocationUpdates(LocationCallback locationCallback) {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Return the current state of the permissions needed.
     */
    public boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermissions(final Activity activity) {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.i(LOG_TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(activity, R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                        }
                    });
        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(Activity activity, final int mainTextStringId,
                              final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                activity.findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /** This Service's Binder **/

    /**
     * The class of the binder returned by `onBind()`.
     */
    class LocationServiceBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }
}
