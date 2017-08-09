package com.example.john.norfolktouring;

import android.app.Fragment;
import android.app.FragmentManager;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.john.norfolktouring.TourLocationListFragment.MilitaryFragment;
import com.example.john.norfolktouring.TourLocationListFragment.MuseumsFragment;
import com.example.john.norfolktouring.TourLocationListFragment.OtherFragment;
import com.example.john.norfolktouring.TourLocationListFragment.ParksFragment;
import com.example.john.norfolktouring.TourLocationListFragment.RestaurantsFragment;
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

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    /*** Member Variables ***/
    private GoogleApiClient mGoogleApiClient;
    private Fragment mCurrentFragment;

    /** Navigation Drawer **/
    private List<String> mCategories;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.left_drawer)   ListView mDrawerList;

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

        // Start ButterKnife.
        ButterKnife.bind(this);

        mCategories = getNavigationDrawerCategories();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        // Give the Google API Client to `PlacesUtils`.
        PlacesUtils.setGoogleApiClient(mGoogleApiClient);

        // Set the adapter for the drawer.
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_category_item, mCategories));
        // Set the drawer's click listener.
        DrawerItemClickListener drawerItemClickListener = new DrawerItemClickListener(this, mDrawerList,
                mCategories) ;
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
            mLocationService.stopLocationUpdates(mLocationCallback);
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
                        mLocationService.stopLocationUpdates(mLocationCallback);
                        break;
                }
                break;
        }
    }

    /** Navigation Drawer **/

    /**
     * Returns a List of the category titles used to populate the Navigation Drawer.
     */
    static List<String> getNavigationDrawerCategories() {
        return new ArrayList<>(Arrays.asList(
                NorfolkTouring.getContext().getResources().getStringArray(R.array.categories_array)
        ));
    }

    /**
     * Click listener for drawer items that opens the corresponding TourLocationListFragments.
     */
    private static class DrawerItemClickListener implements ListView.OnItemClickListener {
        private ListView mDrawerList;
        private DrawerLayout mDrawerLayout;
        private MainActivity mActivity;
        private List<String> mCategories;

        DrawerItemClickListener(MainActivity activity, ListView drawerList,
                                List<String> categories/*, ActionBar actionBar*/){
            this.mActivity = activity;
            this.mDrawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);
            this.mDrawerList = drawerList;
            this.mCategories = categories;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

        /**
         * Swaps fragments in the main content view
         */
        void selectItem(int position) {
            Fragment fragment = null;
            String category = mCategories.get(position);
            // Note that these must be in the same order as listed in @arrays/categories_array.
            switch (category){
                case "Parks":
                    fragment = new ParksFragment();
                    break;
                case "Museums":
                    fragment = new MuseumsFragment();
                    break;
                case "Military":
                    fragment = new MilitaryFragment();
                    break;
                case "Restaurants":
                    fragment = new RestaurantsFragment();
                    break;
                case "Other":
                    fragment = new OtherFragment();
                    break;
            }

            // Insert the fragment by replacing any existing fragment.
            FragmentManager fragmentManager = mActivity.getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment, TourLocationListFragment.FRAGMENT_LABEL)
                    .addToBackStack(TourLocationListFragment.FRAGMENT_LABEL)
                    .commit();

            // Highlight the selected item, update the title, and close the drawer.
            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
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