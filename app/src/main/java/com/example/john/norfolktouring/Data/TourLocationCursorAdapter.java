package com.example.john.norfolktouring.Data;

import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.john.norfolktouring.MainActivity;
import com.example.john.norfolktouring.NavigationIconClickListeners.DirectionsIconClickListener;
import com.example.john.norfolktouring.NavigationIconClickListeners.MapIconClickListener;
import com.example.john.norfolktouring.R;
import com.example.john.norfolktouring.TourLocation;
import com.example.john.norfolktouring.TourLocationDetailFragment;
import com.example.john.norfolktouring.Utils.PlacesUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 9/8/2017.
 */

public class TourLocationCursorAdapter extends RecyclerView.Adapter<TourLocationCursorAdapter.TourLocationViewHolder> {
    private Cursor mCursor;
    private ArrayList<TourLocation> mTourLocations;
    private MainActivity mActivity;
    /**
     * The current device location.
     */
    private Location mCurrentLocation;

    public TourLocationCursorAdapter(MainActivity activity,
                                     Location deviceLocation,
                                     ArrayList<TourLocation> tourLocations) {
        mActivity = activity;
        mCurrentLocation = deviceLocation;
        mTourLocations = tourLocations;
    }

    @Override
    public TourLocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get the RecyclerView item layout
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View view = inflater.inflate(R.layout.location, parent, false);
        return new TourLocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TourLocationViewHolder holder, int position) {
        // Get the TourLocation object located at this position in the cursor.
        final TourLocation currentTourLocation = mTourLocations.get(position);
        /*getTourLocationFromDatabaseData(mCursor, position)*/

        // Set the image resource (select the first-or these summary views).
        holder.locationImageView.setImageResource(currentTourLocation.getResourceImages().get(0));

        // Set location name.
        holder.locationNameView.setText(currentTourLocation.getLocationName());

        // Set the rating (represented as stars).
        int rating = currentTourLocation.getRating();
        // If the rating is known or should be known soon, show the stars.
        if (rating != TourLocation.RATING_NOT_DETERMINED || mActivity.IsWifiCellEnabled()) {
            holder.ratingView.setVisibility(View.VISIBLE);
            for (int starIndx = 0; starIndx < 5; starIndx++) {
                ImageView starView = (ImageView) holder.ratingView.getChildAt(starIndx);
                if (starIndx < rating)
                    starView.setImageResource(R.drawable.ic_star_black_24dp);
                else
                    starView.setImageResource(R.drawable.ic_star_border_black_24dp);
            }
        }// If the rating is not known and wireless data is disabled, do not show stars.
        else {
            holder.ratingView.setVisibility(View.GONE);
        }

        // Set the open status (whether this location is currently open).
        Boolean locationIsOpen = currentTourLocation.getOpenNow();
        if (locationIsOpen == null) {
            holder.openStatusView.setText(R.string.open_status_unavailable);
            // The open status should only be hidden if wireless data is disabled and
            // the open status is unknown.
        } else if (locationIsOpen)
            holder.openStatusView.setText(R.string.location_open);
        else
            holder.openStatusView.setText(R.string.location_closed);

        // Set the distance text for this location.
        final Location location = currentTourLocation.getLocation();
        mActivity.setDistanceText(holder.locationDistanceView, location, mCurrentLocation);
        if (location != null && mCurrentLocation != null)
            holder.locationDistanceView.setVisibility(View.VISIBLE);
        else
            holder.locationDistanceView.setVisibility(View.GONE);

        // Handle the Google Maps view and Route Plan view.
        if (mActivity.IsWifiCellEnabled()) {
            holder.googleMapsView.setVisibility(View.VISIBLE);
            holder.googleMapsRoutePlanView.setVisibility(View.VISIBLE);
            // Set a click listener on the Google Maps icon and text.
            holder.googleMapsView.setOnClickListener(new MapIconClickListener(mActivity, currentTourLocation));
            // Set a click listener on the Google Maps Route Plan icon and text.
            holder.googleMapsRoutePlanView.setOnClickListener(
                    new DirectionsIconClickListener(mActivity, currentTourLocation, mCurrentLocation));
        } else {
            holder.googleMapsView.setVisibility(View.GONE);
            holder.googleMapsRoutePlanView.setVisibility(View.GONE);
        }

        // Set a click listener for a detail view of this tour location.
        TourLocationClickListener detailViewClickListener =
                new TourLocationClickListener(mActivity, currentTourLocation);
        holder.rootView.setOnClickListener(detailViewClickListener);
    }

    private static TourLocation getTourLocationFromDatabaseData(Cursor cursor, int position) {
        // Move the cursor to the correct position (mCursor.moveToPosition(position) is not correct)
        // TODO: Account for invalid values for `position` using `mCursor.moveToNext()`?
        cursor.moveToFirst();
        // The ID field of the `TourLocation` corresponding to the current row.
        int previousTourLocationId = getTourLocationId(cursor);
        int currentPosition = 0;
        int tourLocationId = getTourLocationId(cursor);
        while (currentPosition < position) {
            cursor.moveToNext();
            tourLocationId = getTourLocationId(cursor);;
            if (tourLocationId != previousTourLocationId) {
                previousTourLocationId = tourLocationId;
                currentPosition++;
            }
        }

        // Retrieve the desired data.
        String locationName = getTourLocationName(cursor);
        String description = getTourLocationDescription(cursor);
        String address = getTourLocationAddress(cursor);
        String contactInfo = getTourLocationContactInfo(cursor);
        // Retrieve resource images and features.
        ArrayList<Integer> resourceImages = new ArrayList<>();
        ArrayList<TourLocation.LocationFeature> features = new ArrayList<>();
        ArrayList<Integer> featureImages = new ArrayList<>();
        int previousResImgId = -1, currentResImgId;
        int previousFeatureId = -1, currentFeatureId;
        while (tourLocationId == previousTourLocationId) {
            currentResImgId = getResImgId(cursor);
            // Add any new resource image.
            if (currentResImgId > previousResImgId) {
                previousResImgId = currentResImgId;
                int resourceImage = getResImg(cursor);
                resourceImages.add(resourceImage);
            }

            currentFeatureId = getFeatureId(cursor);
            // Every row may contain a different feature image.
            int featureImg = getFeatureResImg(cursor);
            if (featureImg != 0) featureImages.add(featureImg);
            // Add any new feature.
            if (currentFeatureId > previousFeatureId) {
                previousFeatureId = currentFeatureId;
                String featureName = getFeatureName(cursor);
                String featureDescription = getFeatureDescription(cursor);
                // currentFeatureId can be greater than previousFeatureId, yet there is no feature in this record.
                if (featureName != null)
                    features.add(new TourLocation.LocationFeature(featureName, featureDescription, featureImages));
                // Discard images for the previous feature.
                featureImages = new ArrayList<>();
            }

            if (cursor.moveToNext())
                tourLocationId = getTourLocationId(cursor);
            else break;
        }

        return new TourLocation(locationName, description,
                resourceImages, address, contactInfo, features);
    }

    public static ArrayList<TourLocation> getTourLocationsFromDatabaseData(Cursor cursor) {
        if (cursor == null) return new ArrayList<>();
        final int numTourLocations = getCursorNumTourLocations(cursor);
        ArrayList<TourLocation> tourLocations = new ArrayList<>(numTourLocations);
        for (int i = 0; i < numTourLocations; i++) {
            TourLocation currentTourLocation = getTourLocationFromDatabaseData(cursor, i);
            tourLocations.add(currentTourLocation);
        }
        return tourLocations;
    }

//    /**
//     * Moves `mCursor` to the first row corresponding to entries
//     * for the `TourLocation` specified by `position`.
//     */
//    void moveCursorToPosition(int tourLocationIdIndex, int position) {
//        mCursor.moveToFirst();
//        // The index of the `TourLocation` corresponding to the current row.
//        int previousTourLocationId = -1;
//        int currentPosition = -1;
//        while (true) {
//            int tourLocationId = mCursor.getInt(tourLocationIdIndex);
//            if (tourLocationId != previousTourLocationId) {
//                previousTourLocationId = tourLocationId;
//                currentPosition++;
//            }
//            if (currentPosition == position)
//                break;
//        }
//    }

    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
//        if (mCursor == null) {
//            return 0;
//        }
        if (mTourLocations != null)
            return mTourLocations.size();
        else
            return 0;
        /*getCursorNumTourLocations()*/
    }

    /**
     * Not every row in `mCursor` corresponds to a unique `TourLocation`, so determining
     * the item count for this `Adapter` cannot be achieved by calling `mCursor.getCount()`.
     */
    public int getCursorNumTourLocations() {
        return getCursorNumTourLocations(mCursor);
    }

    public static int getCursorNumTourLocations(Cursor cursor) {
        int originalCursorPosition = cursor.getPosition();
        int previousTourLocationId = -1;
        int tourLocationId;
        int numTourLocations = 0;
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            tourLocationId = getTourLocationId(cursor)/*mCursor.getInt(tourLocationIdIndex)*/;
            if (tourLocationId != previousTourLocationId) {
                previousTourLocationId = tourLocationId;
                numTourLocations++;
            }
        }
        cursor.moveToPosition(originalCursorPosition);
        return numTourLocations;
    }

    /**
     * Records a device location update for this `Adapter`.
     *
     * @param location The new device location.
     */
    public void updateLocation(Location location) {
        mCurrentLocation = location;
        // Refresh the views using the new location.
        notifyDataSetChanged();
    }

    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public Cursor swapCursor(Cursor c) {
        // Check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // Nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c;

        // If this cursor points to any data, notify the adapter of the new data.
        if (c != null) {
            this.notifyDataSetChanged();
        }

        // Extract the `TourLocation`s from the cursor.
        mTourLocations = getTourLocationsFromDatabaseData(mCursor);
//        final int numTourLocations = getCursorNumTourLocations();
//        mTourLocations = new ArrayList<>(numTourLocations);
//        for (int i = 0; i < numTourLocations; i++) {
//            TourLocation currentTourLocation = getTourLocationFromDatabaseData(mCursor, i);
//            mTourLocations.add(currentTourLocation);
//        }

        if (mActivity.IsWifiCellEnabled())
            PlacesUtils.getInfoForTourLocationsIfNeeded(mActivity, mTourLocations);

        return temp;
    }

    /**
     * Getters
     */

    public ArrayList<TourLocation> getTourLocations() {return mTourLocations;}
    public Cursor getCursor() {return mCursor;}

    /**
     * Cursor Getters
     */
    // TODO: Flatten these.
    /* TourLocationEntry data */
    private int getTourLocationId() {
        return getTourLocationId(mCursor);
    }
    private static int getTourLocationId(Cursor cursor) {
        int indx = getTourLocationIdIndex(cursor);
        return cursor.getInt(indx);
    }
    private static int getTourLocationIdIndex(Cursor cursor) {
        String colName = TourLocationContract.TourLocationEntry._ID;
        return cursor.getColumnIndex(colName);
    }

    private String getTourLocationName() {
        return getTourLocationName(mCursor);
    }
    private static String getTourLocationName(Cursor cursor) {
        return cursor.getString(getTourLocationNameIndex(cursor));
    }
    private static int getTourLocationNameIndex(Cursor cursor) {
        return cursor.getColumnIndex(TourLocationContract.TourLocationEntry.QUALIFIED_COLUMN_LOCATION_NAME);
    }

    private String getTourLocationDescription() {
        return getTourLocationDescription(mCursor);
    }
    private static String getTourLocationDescription(Cursor cursor) {
        return cursor.getString(getTourLocationDescriptionIndex(cursor));
    }
    private static int getTourLocationDescriptionIndex(Cursor cursor) {
        return cursor.getColumnIndex(TourLocationContract.TourLocationEntry.QUALIFIED_COLUMN_LOCATION_DESCRIPTION);
    }

    private String getTourLocationAddress() {
        return getTourLocationAddress(mCursor);
    }
    private static String getTourLocationAddress(Cursor cursor) {
        return cursor.getString(getTourLocationAddressIndex(cursor));
    }
    private static int getTourLocationAddressIndex(Cursor cursor) {
        return cursor.getColumnIndex(TourLocationContract.TourLocationEntry.QUALIFIED_COLUMN_LOCATION_ADDRESS);
    }

    private String getTourLocationContactInfo() {
        return getTourLocationContactInfo(mCursor);
    }
    private static String getTourLocationContactInfo(Cursor cursor) {
        return cursor.getString(getTourLocationContactInfoIndex(cursor));
    }
    private static int getTourLocationContactInfoIndex(Cursor cursor) {
        return cursor.getColumnIndex(TourLocationContract.TourLocationEntry.QUALIFIED_COLUMN_LOCATION_CONTACT_INFO);
    }

    /* TourLocationResourceImage data */
    private int getResImgId() {
        return getResImgId(mCursor);
    }
    private static int getResImgId(Cursor cursor) {
        int indx = getResImgIdIndex(cursor);
        return cursor.getInt(indx);
    }
    private static int getResImgIdIndex(Cursor cursor) {
        return cursor.getColumnIndex(TourLocationContract.TourLocationResourceImage.UNIQUE_ID);
    }

    private int getResImg() {
        return getResImg(mCursor);
    }
    private static int getResImg(Cursor cursor) {
        return cursor.getInt(getResImgIndex(cursor));
    }
    private static int getResImgIndex(Cursor cursor) {
        return cursor.getColumnIndex(TourLocationContract.TourLocationResourceImage.QUALIFIED_COLUMN_RESOURCE_IMAGE);
    }

    /* LocationFeatureEntry data */
    private int getFeatureId() {
        return getFeatureId(mCursor);
    }
    private static int getFeatureId(Cursor cursor) {
        int indx = getFeatureIdIndex(cursor);
        return cursor.getInt(indx);
    }
    private static int getFeatureIdIndex(Cursor cursor) {
        return cursor.getColumnIndex(TourLocationContract.LocationFeatureEntry.UNIQUE_ID);
    }

    private String getFeatureName() {
        return getFeatureName(mCursor);
    }
    private static String getFeatureName(Cursor cursor) {
        return cursor.getString(getFeatureNameIndex(cursor));
    }
    private static int getFeatureNameIndex(Cursor cursor) {
        return cursor.getColumnIndex(TourLocationContract.LocationFeatureEntry.QUALIFIED_COLUMN_FEATURE_NAME);
    }

    private String getFeatureDescription() {
        return getFeatureDescription(mCursor);
    }
    private static String getFeatureDescription(Cursor cursor) {
        return cursor.getString(getFeatureDescriptionIndex(cursor));
    }
    private static int getFeatureDescriptionIndex(Cursor cursor) {
        return cursor.getColumnIndex(TourLocationContract.LocationFeatureEntry.QUALIFIED_COLUMN_FEATURE_DESCRIPTION);
    }

    /* LocationFeatureResourceImage data */
    private int getFeatureResImg() {
        return getFeatureResImg(mCursor);
    }
    private static int getFeatureResImg(Cursor cursor) {
        return cursor.getInt(getFeatureResImgIndex(cursor));
    }
    private static int getFeatureResImgIndex(Cursor cursor) {
        return cursor.getColumnIndex(TourLocationContract.LocationFeatureResourceImage.QUALIFIED_COLUMN_FEATURE_IMAGE);
    }

    /**
     * Nested Classes
     */

    /**
     * Class for the `RecyclerView` items.
     */
    class TourLocationViewHolder extends RecyclerView.ViewHolder {
        View rootView;
        ImageView locationImageView;
        TextView locationNameView;
        LinearLayout ratingView;
        TextView openStatusView;
        TextView locationDistanceView;
        View googleMapsView;
        View googleMapsRoutePlanView;

        TourLocationViewHolder(View itemView) {
            super(itemView);
            // Get the root `View`.
            rootView = itemView;
            // Get image.
            locationImageView = (ImageView) itemView.findViewById(R.id.location_image);
            // Get name.
            locationNameView = (TextView) itemView.findViewById(R.id.location_name);
            // Get rating.
            ratingView = (LinearLayout) itemView.findViewById(R.id.location_rating_main_view);
            // Get open status.
            openStatusView = (TextView) itemView.findViewById(R.id.location_open_status_main_view);
            // Get distance text.
            locationDistanceView = (TextView) itemView.findViewById(R.id.location_distance_main_view);
            // Get Google Maps icon and text.
            googleMapsView = itemView.findViewById(R.id.google_maps_main_view);
            // Get Google Maps Route Plan icon and text.
            googleMapsRoutePlanView = itemView.findViewById(R.id.google_maps_route_plan_main_view);
        }
    }

    /**
     * Clicking on one of these Views opens a detailed view for the corresponding TourLocation.
     */
    static class TourLocationClickListener implements View.OnClickListener {
        private MainActivity mActivity;
        private TourLocation mTourLocation;

        TourLocationClickListener(MainActivity activity, TourLocation tourLocation) {
            this.mActivity = activity;
            this.mTourLocation = tourLocation;
        }

        @Override
        public void onClick(View v) {
            // Create a detail fragment associated with this position (a location).
            Bundle bundle = new Bundle();
            bundle.putParcelable("location", mTourLocation);
            TourLocationDetailFragment detailFragment = new TourLocationDetailFragment();
            detailFragment.setArguments(bundle);

            // Make this detail fragment the current fragment.
            mActivity.getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, detailFragment,
                            TourLocationDetailFragment.FRAGMENT_LABEL)
                    // Add this transaction to the back stack.
                    .addToBackStack(TourLocationDetailFragment.FRAGMENT_LABEL)
                    .commit();
        }
    }
}
