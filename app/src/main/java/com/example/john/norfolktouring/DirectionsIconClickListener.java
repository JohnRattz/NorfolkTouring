package com.example.john.norfolktouring;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.example.john.norfolktouring.MainActivity;
import com.example.john.norfolktouring.NorfolkTouring;
import com.example.john.norfolktouring.TourLocation;
import com.example.john.norfolktouring.VolleyRequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by John on 7/6/2017.
 */
public class DirectionsIconClickListener implements View.OnClickListener {
    private MainActivity mActivity;
    private StringBuilder mOriginAddress = new StringBuilder();
    private String mDestinationAddress;

    private final float DEFAULT_ZOOM = 17;
    private final String LOG_TAG = this.getClass().getCanonicalName();

    public DirectionsIconClickListener(MainActivity activity, TourLocation tourLocation,
                                       Location deviceLocation) {
        mActivity = activity;
        // Set `mOriginAddress`.
        getAddressFromLocation(mOriginAddress, deviceLocation);
        mDestinationAddress = tourLocation.getAddress();
    }

    /**
     * Retrieves an address from a `Location` object.
     */
    private void getAddressFromLocation(final StringBuilder deviceAddress, Location deviceLocation) {
        if (deviceLocation != null) {
            // Create the URI query String.
            String uriPath = "https://maps.googleapis.com/maps/api/geocode/json";
            String uriParams = "?latlng=" + String.format("%f,%f",
                    deviceLocation.getLatitude(), deviceLocation.getLongitude()) +
                    "&key=" + NorfolkTouring.GEO_DATA_WEB_API_KEY;
            String uriString = uriPath + uriParams;
            // Create the query.
            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            JSONObject response = null;
            Map<String, String> jsonParams = new HashMap<String, String>();
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    uriString,
                    new JSONObject(jsonParams),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response != null) {
                                    String resultString =
                                            response.getJSONArray("results")
                                                    .getJSONObject(0)
                                                    .getString("formatted_address");
                                    deviceAddress.append(resultString);
                                }
                            } catch (JSONException e) {
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
    }

    @Override
    public void onClick(View v) {
        // Only plan a route if both the origin and destination `Location`s have been determined.
        if (mOriginAddress.length() != 0 && mDestinationAddress != null) {
            // Build the URI query string.
            String uriPath = "https://www.google.com/maps/dir/";
            String uriParams =
                    "?api=1" +
                    "&origin=" + mOriginAddress.toString().replace(" ", "+").replace(",", "") +
                    "&destination=" + mDestinationAddress.replace(" ", "+").replace(",", "") +
                    "&travelmode=driving";
            Uri queryURI = Uri.parse(uriPath + uriParams);
            // Open the map.
            Intent intent = new Intent(Intent.ACTION_VIEW, queryURI);
            startActivity(mActivity, intent, null);
        }
    }
}
