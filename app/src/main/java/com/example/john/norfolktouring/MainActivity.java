package com.example.john.norfolktouring;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.john.norfolktouring.Location.LocationService;
import com.example.john.norfolktouring.Location.NearestLocationNotificationUtilities;
import com.example.john.norfolktouring.Location.NearestLocationTasks;
import com.example.john.norfolktouring.TourLocationListFragment.MilitaryFragment;
import com.example.john.norfolktouring.TourLocationListFragment.MuseumsFragment;
import com.example.john.norfolktouring.TourLocationListFragment.OtherFragment;
import com.example.john.norfolktouring.TourLocationListFragment.ParksFragment;
import com.example.john.norfolktouring.TourLocationListFragment.RestaurantsFragment;
import com.example.john.norfolktouring.TourLocationListFragment.TourLocationListFragment;
import com.example.john.norfolktouring.Utils.InfoByIdsTask;
import com.example.john.norfolktouring.Utils.PlacesUtils;
import com.example.john.norfolktouring.Utils.SharedPreferencesUtils;
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
        ActivityCompat.OnRequestPermissionsResultCallback,
        InfoByIdsTask.InfoByIdResultCallback,
        SharedPreferences.OnSharedPreferenceChangeListener,
        LocationService.IReceivesLocationUpdates {
    /*** Memtir Variables ***/
    private GoogleApiClient mGoogleApiClient;
    private Fragment mCurrentFragment;
    // Denotes whether wifi and cell data usage is allowed.
    private boolean mWifiCellEnabled;

    /**
     * Navigation Drawer
     **/
    private List<String> mCategories;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.left_drawer)
    ListView mDrawerList;

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

            // Log that we are receiving location updates.
            mReceivingLocationUpdates = true;

            // Get the current Fragment (there is only ever one showing in this Activity).
            Fragment currentFragment = getCurrentFragment();
            mCurrentLocation = locationResult.getLastLocation();
            if (currentFragment instanceof TourLocationListFragment) {
                ((TourLocationListFragment) currentFragment).locationCallback(mCurrentLocation);
            } else if (currentFragment instanceof TourLocationDetailFragment) {
                ((TourLocationDetailFragment) currentFragment).locationCallback();
            }
        }
    };

    /**
     * Constants
     **/

    // Arbitrary integer constant denoting a request for access to fine location.
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private static final String LOG_TAG = MainActivity.class.getCanonicalName();

    // These are used for processing PendingIntents issued by widgets.
    public static final String EXTRA_CATEGORY_INDX = "com.example.john.norfolktouring.extra.EXTRA_CATEGORY_INDX";
    public static final int INVALID_CATEGORY_INDX = -1;
    private DrawerItemClickListener mDrawerItemClickListener;

    /*** Methods ***/

    public boolean IsWifiCellEnabled() {
        return mWifiCellEnabled;
    }

    /**
     * Sets appropriate text for a TextView denoting the distance to a location.
     *
     * @param distanceView   The View for which text indicating the distance needs to be set.
     * @param location       The target location.
     * @param deviceLocation The location of the device.
     */
    public void setDistanceText(TextView distanceView, Location location, Location deviceLocation) {
        if (distanceView != null) {
            // If both the target location and the device's location are known...
            if (location != null && deviceLocation != null) {
                // Set the appropriate text in the corresponding `View` in the list.
                String formatString = getString(R.string.location_distance_main_view);
                // If wireless data is disabled, note that this information may not be reliable.
                if (!IsWifiCellEnabled())
                    formatString =
                            formatString.concat(" (data disabled)");
                distanceView.setText(
                        String.format(formatString, (int) deviceLocation.distanceTo(location)));
            }
        }
    }

    /**
     * Shared Preferences
     **/

    /**
     * Initializes shared preferences and registers this Activity as a listener for changes.
     */
    private void setupSharedPreferences() {
        // TODO: Are these the right shared preferences? Should Activity context be used instead (this)?
        SharedPreferences sharedPreferences = SharedPreferencesUtils.getDefaultSharedPreferences();
        mWifiCellEnabled = SharedPreferencesUtils.getWifiCellEnabledSharedPreference(sharedPreferences);
                /*sharedPreferences.getBoolean(
                getString(R.string.pref_enable_wifi_cell_data_usage_key),
                getResources().getBoolean(R.bool.pref_enable_wifi_cell_data_usage_default));*/
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Called when the shared preferences change.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SharedPreferencesUtils.WIFI_CELL_ENABLED_SHARED_PREFERENCE_KEY)) {
            mWifiCellEnabled = SharedPreferencesUtils.getWifiCellEnabledSharedPreference(sharedPreferences);
            // Reconnecting is handled in `onResume()`.
            if (!mWifiCellEnabled) {
                stopLocationUpdates();
                unbindLocationService();
            }
        }
    }

    /** Intents **/

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        int categoryIndx = getIntent().getIntExtra(EXTRA_CATEGORY_INDX, INVALID_CATEGORY_INDX);
        if (categoryIndx != INVALID_CATEGORY_INDX) {
            // First clear the Fragment back stack.
            FragmentManager fm = getFragmentManager();
            for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                fm.popBackStackImmediate();
            }
            mDrawerItemClickListener.selectItem(categoryIndx);
        }
        // Note that this is the source of current location information for NearestLocationTasks.
        NearestLocationTasks.setLocationReceiver(this);
        // Launch the nearest location notification service.
        NearestLocationNotificationUtilities.scheduleNearestLocationNotifications(this);
    }

    /**
     * Lifecycle Methods
     **/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start ButterKnife.
        ButterKnife.bind(this);

        setupSharedPreferences();

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
        mDrawerItemClickListener = new DrawerItemClickListener(this, mDrawerList,
                mCategories);
        mDrawerList.setOnItemClickListener(mDrawerItemClickListener);

        // Set the introductory `Fragment`.
        IntroductoryFragment introductoryFragment = new IntroductoryFragment();
        // Make this introductory fragment the current fragment.
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, introductoryFragment, IntroductoryFragment.FRAGMENT_LABEL)
                .commit();

        // If this Activity was launched by a widget, a particular category may need to be viewed.
        int categoryIndx = getIntent().getIntExtra(EXTRA_CATEGORY_INDX, INVALID_CATEGORY_INDX);
        if (categoryIndx != INVALID_CATEGORY_INDX)
            mDrawerItemClickListener.selectItem(categoryIndx);

        // Note that this is the source of current location information for NearestLocationTasks.
        NearestLocationTasks.setLocationReceiver(this);
        // Launch the nearest location notification service.
        NearestLocationNotificationUtilities.scheduleNearestLocationNotifications(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Intent nearestLocationNotificationIntent =
//                new Intent(this, NearestLocationIntentService.class);
//        nearestLocationNotificationIntent.setAction(
//                NearestLocationIntentService.ACTION_CLOSEST_LOCATION_NOTIFICATION);
//        startService(nearestLocationNotificationIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        if (mWifiCellEnabled)
            // Connect to `LocationService` to receive location updates.
            LocationService.bind(this, mLocationServiceConnection);
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
        stopLocationUpdates();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindLocationService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        // TODO: Stop receiving nearest location notifications (use NearestLocationNotificationUtilities).
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

    /**
     * Menu
     **/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.preferences_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        private MainActivity mActivity;
        private DrawerLayout mDrawerLayout;
        private ListView mDrawerList;
        private List<String> mCategories;

        DrawerItemClickListener(MainActivity activity, ListView drawerList,
                                List<String> categories) {
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
            String fragmentLabel = null;
            Fragment currentFragment = MainActivity.this.getCurrentFragment();
            boolean selectedFragmentIsCurrentFragment = false;
            FragmentManager fragmentManager = mActivity.getFragmentManager();
            String category = mCategories.get(position);

            // Note that these must be in the same order as listed in @arrays/categories_array.
            switch (category) {
                case "Parks":
                    fragmentLabel = ParksFragment.FRAGMENT_LABEL;
                    // If the current fragment is an instance of this Fragment, we will do nothing.
                    if (currentFragment instanceof ParksFragment) {
                        selectedFragmentIsCurrentFragment = true;
                    } else {
                        // Check for an instance of this Fragment (in this case on the back stack)
                        fragment = fragmentManager.findFragmentByTag(fragmentLabel);
                        // If there is none, create a new instance.
                        if (fragment == null) fragment = new ParksFragment();
                    }
                    break;
                case "Museums":
                    fragmentLabel = MuseumsFragment.FRAGMENT_LABEL;
                    if (currentFragment instanceof MuseumsFragment) {
                        selectedFragmentIsCurrentFragment = true;
                    } else {
                        fragment = fragmentManager.findFragmentByTag(fragmentLabel);
                        if (fragment == null) fragment = new MuseumsFragment();
                    }
                    break;
                case "Military":
                    fragmentLabel = MilitaryFragment.FRAGMENT_LABEL;
                    if (currentFragment instanceof MilitaryFragment) {
                        selectedFragmentIsCurrentFragment = true;
                    } else {
                        fragment = fragmentManager.findFragmentByTag(fragmentLabel);
                        if (fragment == null) fragment = new MilitaryFragment();
                    }
                    break;
                case "Restaurants":
                    fragmentLabel = RestaurantsFragment.FRAGMENT_LABEL;
                    if (currentFragment instanceof RestaurantsFragment) {
                        selectedFragmentIsCurrentFragment = true;
                    } else {
                        fragment = fragmentManager.findFragmentByTag(fragmentLabel);
                        if (fragment == null) fragment = new RestaurantsFragment();
                    }
                    break;
                case "Other":
                    fragmentLabel = OtherFragment.FRAGMENT_LABEL;
                    if (currentFragment instanceof OtherFragment) {
                        selectedFragmentIsCurrentFragment = true;
                    } else {
                        fragment = fragmentManager.findFragmentByTag(fragmentLabel);
                        if (fragment == null) fragment = new OtherFragment();
                    }
                    break;
            }

            if (!selectedFragmentIsCurrentFragment) {
                // Insert the fragment by replacing any existing fragment.
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment, fragmentLabel)
                        .addToBackStack(fragmentLabel)
                        .commit();
            }

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

    /**
     * Stop processing location updates.
     */
    private void stopLocationUpdates() {
        if (mLocationServiceBound && mReceivingLocationUpdates)
            mLocationService.stopLocationUpdates(mLocationCallback);
    }

    public LocationService getLocationService() {
        return mLocationService;
    }

    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    private void unbindLocationService() {
        if (mLocationServiceBound) {
            // This will terminate the local Service and stop location updates completely.
            unbindService(mLocationServiceConnection);
            mLocationServiceBound = false;
        }
    }

    /**
     * Callback for `InfoByIdsTask`.
     */
    @Override
    public void infoByIdResultCallback() {
        // TODO: If a tablet view is to be supported, there may be multiple callbacks to call.
        if (mCurrentFragment instanceof InfoByIdsTask.InfoByIdResultCallback)
            ((InfoByIdsTask.InfoByIdResultCallback) mCurrentFragment).infoByIdResultCallback();
    }
}