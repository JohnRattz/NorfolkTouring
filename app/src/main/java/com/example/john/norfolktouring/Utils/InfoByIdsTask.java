package com.example.john.norfolktouring.Utils;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.example.john.norfolktouring.NorfolkTouring;
import com.example.john.norfolktouring.TourLocation;
import com.example.john.norfolktouring.TourLocationAdapter;
import com.example.john.norfolktouring.VolleyRequestQueue;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by John on 7/18/2017.
 */

/**
 * Acquires location, hours of operation, rating, and the website for `tourLocations`
 * using URI queries to the Google Places API and the Volley networking library.
 */
class InfoByIdsTask extends AsyncTask<String, Void, Void> {
    /*** Member Variables ***/
    private Activity mActivity;
    private List<TourLocation> mTourLocations;
    private TourLocationAdapter mAdapter;

    // Constants
    private static final String LOG_TAG = InfoByIdsTask.class.getCanonicalName();

    /*** Methods ***/

    InfoByIdsTask(Activity activity, List<TourLocation> tourLocations,
                  TourLocationAdapter adapter) {
        mActivity = activity;
        mTourLocations = tourLocations;
        mAdapter = adapter;
    }

    @Override
    protected Void doInBackground(String... params) {
        String uriPath = "https://maps.googleapis.com/maps/api/place/details/json";
        for (int tourLocationIndx = 0; tourLocationIndx < params.length; tourLocationIndx++) {
            final String currentPlaceID = params[tourLocationIndx];
            String uriParams = "?placeid=" + currentPlaceID +
                    "&key=" + NorfolkTouring.GEO_DATA_WEB_API_KEY;
            String uriString = uriPath + uriParams;
            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            JSONObject response = null;
            Map<String, String> jsonParams = new HashMap<String, String>();
            final int finalTourLocationIndx = tourLocationIndx;
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    uriString,
                    new JSONObject(jsonParams),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response != null) {
                                    // Get a reference to the current `TourLocation`.
                                    TourLocation currentTourLocation =
                                            mTourLocations.get(finalTourLocationIndx);
                                    // Retrieve the result (main contents).
                                    JSONObject result = response.getJSONObject("result");
                                    // Acquire the `Location`.
                                    JSONObject geometryJSON = result.getJSONObject("geometry");
                                    JSONObject locationJSON = geometryJSON.getJSONObject("location");
                                    double latitude = locationJSON.getDouble("lat");
                                    double longitude = locationJSON.getDouble("lng");
                                    Location location = new Location("");
                                    location.setLatitude(latitude);
                                    location.setLongitude(longitude);
                                    currentTourLocation.setLocation(location);
                                    // Acquire the hours of operation.
                                    try {
                                        JSONObject openingHoursJSON =
                                                result.getJSONObject("opening_hours");
                                        // Determine whether this location is currently open.
                                        boolean openNow = openingHoursJSON.getBoolean("open_now");
                                        currentTourLocation.setOpenNow(openNow);
                                        // Get the hours of operation.
                                        JSONArray periods =
                                                openingHoursJSON.getJSONArray("periods");
                                        for (int periodIndx = 0;
                                             periodIndx < periods.length(); periodIndx++) {
                                            JSONObject currentDayHoursJSON =
                                                    periods.getJSONObject(periodIndx);
                                            // Get the opening hours.
                                            JSONObject openJSON =
                                                    currentDayHoursJSON.getJSONObject("open");
                                            int day = openJSON.getInt("day");
                                            int openTime = openJSON.getInt("time");
                                            // Get the closing hours.
                                            JSONObject closeJSON =
                                                    currentDayHoursJSON.getJSONObject("close");
                                            int closeTime = closeJSON.getInt("time");
                                            currentTourLocation.setHours(day, openTime, closeTime);
                                        }
                                    } catch (JSONException e) {
                                        // This `Place` has no associated hours of operation.
                                        currentTourLocation.setOpenNow(null);
                                    }
                                    // Acquire the rating.
                                    try {
                                        int rating = result.getInt("rating");
                                        currentTourLocation.setRating(rating);
                                    } catch (JSONException e) {
                                        // This `Place` has no associated rating.
                                    }
                                    // Acquire the website.
                                    try {
                                        String website = result.getString("website");
                                        currentTourLocation.setWebsite(website);
                                    } catch (JSONException e) {
                                        // This `Place` has no associated website.
                                    }
                                }
                            } catch (JSONException e) {
                                // This should only happen if assumptions about the returned
                                // JSON structure are invalid.
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(LOG_TAG, "Error occurred ", error);
                        }
                    });
            // Add the request to the request queue.
            VolleyRequestQueue.getInstance(mActivity).addToRequestQueue(request);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        // Signal the relevant adapter to refresh its display.
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }
}
