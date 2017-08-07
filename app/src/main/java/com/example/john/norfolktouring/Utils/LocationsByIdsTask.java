package com.example.john.norfolktouring.Utils;

import android.location.Location;
import android.os.AsyncTask;

import com.example.john.norfolktouring.TourLocation;
import com.example.john.norfolktouring.TourLocationAdapter;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by John on 7/3/2017.
 */

/**
 * Not used, but shows how to retrieve latitude and longitude for a place
 * from the Google Places API for Android (no URI queries here).
 */
class LocationsByIdsTask extends AsyncTask<String, Void, Void> {
    private GoogleApiClient mGoogleApiClient;
    private List<TourLocation> mTourLocations;
    private TourLocationAdapter mAdapter;

    // Constants
    private static final String LOG_TAG = LocationsByIdsTask.class.getCanonicalName();

    LocationsByIdsTask(List<TourLocation> tourLocations, TourLocationAdapter adapter) {
        mGoogleApiClient = PlacesUtils.getGoogleApiClient();
        mTourLocations = tourLocations;
        mAdapter = adapter;
    }

    @Override
    protected Void doInBackground(String... params) {
        // Get the `Places` corresponding to the place names in `params`.
        PendingResult<PlaceBuffer> pendingResult =
                Places.GeoDataApi.getPlaceById(mGoogleApiClient, params);
        PlaceBuffer placeBuffer = pendingResult.await();
        // Set the `Location` objects for the `TourLocation`s .
        for (int i = 0; i < placeBuffer.getCount(); i++) {
            Place place = placeBuffer.get(i);
            LatLng latLng = place.getLatLng();
            Location location = new Location("");
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);
            mTourLocations.get(i).setLocation(location);
        }
        placeBuffer.release();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        // Signal the relevant adapter to refresh its display.
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }
}
