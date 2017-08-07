package com.example.john.norfolktouring.NavigationIconClickListeners;

import android.location.Location;
import android.view.View;

import com.example.john.norfolktouring.MainActivity;
import com.example.john.norfolktouring.R;
import com.example.john.norfolktouring.TourLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by John on 7/6/2017.
 */

public class MapIconClickListener implements View.OnClickListener {
    private MainActivity mActivity;
    private TourLocation mTourLocation;
    private Location mLocation;

    private final float DEFAULT_ZOOM = 17;
    private final String LOG_TAG = this.getClass().getCanonicalName();

    public MapIconClickListener(MainActivity activity, TourLocation tourLocation) {
        mActivity = activity;
        mTourLocation = tourLocation;
        mLocation = tourLocation.getLocation();
    }

    private OnMapReadyCallback mMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            // If we have permission to request the device's location, mark it on the map.
            if (mActivity.getLocationService().checkPermissions()) {
                //noinspection MissingPermission
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
            // If the `Location` of this `TourLocation` has been determined, zoom to it.
            LatLng locationLatLng =
                    new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    locationLatLng,
                    DEFAULT_ZOOM));
            // Place a marker at this location.
            googleMap.addMarker(new MarkerOptions()
                    .title(mTourLocation.getLocationName())
                    .position(locationLatLng));
        }
    };

    @Override
    public void onClick(View v) {
        // If the `Location` of this `TourLocation` has been determined,
        // show it on the map.
        if (mLocation != null) {
            // Replace the current Fragment with a Google Map Fragment.
            MapFragment mapFragment = new MapFragment();
            mActivity.getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, mapFragment,
                            "MapFragment")
                    // Add this transaction to the back stack.
                    .addToBackStack("MapFragment")
                    .commit();
            mapFragment.getMapAsync(mMapReadyCallback);
        }
    }
}
