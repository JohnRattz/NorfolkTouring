package com.example.john.norfolktouring;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.john.norfolktouring.TourLocationListFragment.TourLocationListFragment;
import com.example.john.norfolktouring.Utils.PlacesUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    /*** Member Variables ***/
    private GoogleApiClient mGoogleApiClient;
    private Fragment mCurrentFragment;

    /** Navigation Drawer **/
    private List<String> mCategories;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    /** Location Updates **/
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

            // Log that we are receiving location updates.
            mReceivingLocationUpdates = true;

            // Get the current Fragment (there is only ever one showing in this Activity).
            Fragment currentFragment = getCurrentFragment();
            mCurrentLocation = locationResult.getLastLocation();
            if (currentFragment instanceof TourLocationListFragment) {
                ((TourLocationListFragment) currentFragment).locationCallback(mCurrentLocation);
            } else if (currentFragment instanceof TourLocationDetailFragment) {
                ((TourLocationDetailFragment) currentFragment).locationCallback(mCurrentLocation);
            }
        }
    };

    // Constants

    // Arbitrary integer constant denoting a request for access to fine location.
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private static final String LOG_TAG = MainActivity.class.getCanonicalName();

    // These are used for processing PendingIntents issued by widgets.
    public static final String EXTRA_CATEGORY_INDX = "com.example.john.norfolktouring.extra.EXTRA_CATEGORY_INDX";
    public static final int INVALID_CATEGORY_INDX = -1;

    /*** Methods ***/

    /**
     * Lifecycle Methods
     **/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCategories = getNavigationDrawerCategories();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        // Give the Google API Client to `PlacesUtils`.
        PlacesUtils.setGoogleApiClient(mGoogleApiClient);

        // Set the adapter for the drawer.
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_category_item, mCategories));
        // Set the drawer's click listener.
        DrawerItemClickListener drawerItemClickListener = new DrawerItemClickListener(this, mDrawerList,
                mCategories, getSupportActionBar()) ;
        mDrawerList.setOnItemClickListener(drawerItemClickListener);

        // Set the introductory `Fragment`.
        IntroductoryFragment introductoryFragment = new IntroductoryFragment();
        // Make this introductory fragment the current fragment.
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, introductoryFragment, IntroductoryFragment.FRAGMENT_LABEL)
                .commit();

        // If this Activity was launched by a widget, a particular category may need to be viewed.
        int categoryIndx = getIntent().getIntExtra(EXTRA_CATEGORY_INDX, INVALID_CATEGORY_INDX);
        if (categoryIndx != INVALID_CATEGORY_INDX)
            drawerItemClickListener.selectItem(categoryIndx);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect to `LocationService` to receive location updates.
        LocationService.bind(this, mLocationServiceConnection);
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();

        // Remove location updates to save battery.
        if (mLocationServiceBound && mReceivingLocationUpdates)
            mLocationService.stopLocationUpdates(this, mLocationCallback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLocationServiceBound) {
            unbindService(mLocationServiceConnection);
            mLocationServiceBound = false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case LocationService.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        Log.i(LOG_TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. `mLocationService.startLocationUpdates()`
                        // gets called in `onResume()` again.
                        break;
                    case RESULT_CANCELED:
                        Log.i(LOG_TAG, "User chose not to make required location settings changes.");
                        mLocationService.stopLocationUpdates(this, mLocationCallback);
                        break;
                }
                break;
        }
    }

    /** Drawer Population Methods **/

    /**
     * Returns a List of the category titles used to populate the Navigation Drawer.
     */
    static List<String> getNavigationDrawerCategories() {
        return new ArrayList<String>(Arrays.asList(
                NorfolkTouring.getContext().getResources().getStringArray(R.array.categories_array)
        ));
    }

    private void addCategoriesToDrawer() {
        for (int categoryIndx = 0; categoryIndx < mCategories.size(); categoryIndx++) {
            String category = mCategories.get(categoryIndx);
            // Create a `View` for this feature.
            View drawerItemView = View.inflate(this, R.layout.drawer_category_item, null);
            mDrawerList.addView(drawerItemView);
        }
    }

    /**
     * Permissions Requests
     **/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionResult");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && mReceivingLocationUpdates) {
                    Log.i(LOG_TAG, "Permission granted, updates requested, starting location updates");
                    mLocationService.startLocationUpdates(this, mLocationCallback);
                }
        }
    }

    /** Google API Client Methods **/

    /**
     * Called when a Google API client fails to connect to the Google servers.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_TAG,
                "Failed to connect to the Google API servers.\n" +
                        "Message: " + connectionResult.getErrorMessage() +
                        "Error code: " + connectionResult.getErrorCode());
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    /**
     * Fragment Methods
     **/

    public void setCurrentFragment(Fragment fragment) {
        mCurrentFragment = fragment;
    }

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

    /**
     * Location Information Methods
     **/

    private void attemptStartLocationUpdates() {
        if (mLocationService.checkPermissions())
            mLocationService.startLocationUpdates(this, mLocationCallback);
        else
            mLocationService.requestPermissions(this);
    }

    public LocationService getLocationService() {
        return mLocationService;
    }

    public Location getCurrentLocation() {
        return mCurrentLocation;
    }
}