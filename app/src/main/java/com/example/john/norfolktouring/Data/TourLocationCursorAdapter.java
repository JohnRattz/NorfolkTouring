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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 9/8/2017.
 */

public class TourLocationCursorAdapter extends RecyclerView.Adapter<TourLocationCursorAdapter.TourLocationViewHolder> {
    private Cursor mCursor;
    private List<TourLocation> mTourLocations;
    private MainActivity mActivity;
    /**
     * The current device location.
     */
    private Location mCurrentLocation;

    public TourLocationCursorAdapter(MainActivity activity,
                                     Location deviceLocation) {
        mActivity = activity;
        mCurrentLocation = deviceLocation;
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
        final TourLocation currentTourLocation = getTourLocationFromDatabaseData(mCursor, position);

        // Set the image resource (select the first for these summary views).
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

    private TourLocation getTourLocationFromDatabaseData(Cursor cursor, int position) {
        // Move the cursor to the correct position (mCursor.moveToPosition(position) is not correct)
        // TODO: Account for invalid values for `position` using `mCursor.moveToNext()`?
        mCursor.moveToFirst();
        // The ID field of the `TourLocation` corresponding to the current row.
        int previousTourLocationId = getTourLocationId();
        int currentPosition = 0;
        int tourLocationId = getTourLocationId();
        while (currentPosition < position) {
            mCursor.moveToNext();
            tourLocationId = getTourLocationId();/*mCursor.getInt(tourLocationIdIndex)*/
            ;
            if (tourLocationId != previousTourLocationId) {
                previousTourLocationId = tourLocationId;
                currentPosition++;
            }
        }

        // Retrieve the desired data.
        String locationName = getTourLocationName()/*mCursor.getString(nameIndex)*/;
        String description = getTourLocationDescription()/*mCursor.getString(descriptionIndex)*/;
        String address = getTourLocationAddress()/*mCursor.getString(addressIndex)*/;
        String contactInfo = getTourLocationContactInfo()/*mCursor.getString(contactInfoIndex)*/;
        // Retrieve resource images and features.
        ArrayList<Integer> resourceImages = new ArrayList<>();
        ArrayList<TourLocation.LocationFeature> features = new ArrayList<>();
        ArrayList<Integer> featureImages = new ArrayList<>();
        int previousResImgId = -1, currentResImgId;
        int previousFeatureId = -1, currentFeatureId;
        while (tourLocationId == previousTourLocationId) {
            currentResImgId = getResImgId()/*mCursor.getInt(resImgIdIndex)*/;
            // Add any new resource image.
            if (currentResImgId > previousResImgId) {
                previousResImgId = currentResImgId;
                int resourceImage = getResImg()/*mCursor.getInt(resImgIndex)*/;
                resourceImages.add(resourceImage);
            }

            currentFeatureId = getFeatureId()/*mCursor.getInt(featureIdIndex)*/;
            // Every row may contain a different feature image.
            int featureImg = getFeatureResImg()/*mCursor.getInt(featureResImgIndex)*/;
            if (featureImg != 0) featureImages.add(featureImg);
            // Add any new feature.
            if (currentFeatureId > previousFeatureId) {
                previousFeatureId = currentFeatureId;
                String featureName = getFeatureName()/*mCursor.getString(featureNameIndex)*/;
                String featureDescription = getFeatureDescription()/*mCursor.getString(featureDescriptionIndex)*/;
                features.add(new TourLocation.LocationFeature(featureName, featureDescription, featureImages));
                // Discard images for the previous feature.
                featureImages = new ArrayList<>();
            }

//            int cursorPos = mCursor.getPosition();
//            int numCursorElems = mCursor.getCount();
//            int numCursorCols = mCursor.getColumnCount();
            if (mCursor.moveToNext())
                tourLocationId = getTourLocationId()/*mCursor.getInt(tourLocationIdIndex)*/;
            else break;
        }

        return new TourLocation(locationName, description,
                resourceImages, address, contactInfo, features);
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
        if (mCursor == null) {
            return 0;
        }
        return getNumTourLocations();
    }

    /**
     * Not every row in `mCursor` corresponds to a unique `TourLocation`, so determining
     * the item count for this `Adapter` cannot be achieved by calling `mCursor.getCount()`.
     */
    private int getNumTourLocations() {
        int originalCursorPosition = mCursor.getPosition();
        int previousTourLocationId = -1;
//        int currentPosition = ;
        int tourLocationId;
        int numTourLocations = 0;
        mCursor.moveToPosition(-1);
        while (mCursor.moveToNext()) {
            tourLocationId = getTourLocationId()/*mCursor.getInt(tourLocationIdIndex)*/;
            if (tourLocationId != previousTourLocationId) {
                previousTourLocationId = tourLocationId;
                numTourLocations++;
            }
        }
        mCursor.moveToPosition(originalCursorPosition);
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


        return temp;
    }

    /**
     * Getters
     */
    /* TourLocationEntry data */
    private int getTourLocationId() {
        int indx = getTourLocationIdIndex();
        return mCursor.getInt(indx);
    }

    private int getTourLocationIdIndex() {
        String colName = TourLocationContract.TourLocationEntry._ID;
        return mCursor.getColumnIndex(colName);
    }

    private String getTourLocationName() {
        return mCursor.getString(getTourLocationNameIndex());
    }

    private int getTourLocationNameIndex() {
        return mCursor.getColumnIndex(TourLocationContract.TourLocationEntry.QUALIFIED_COLUMN_LOCATION_NAME);
    }

    private String getTourLocationDescription() {
        return mCursor.getString(getTourLocationDescriptionIndex());
    }

    private int getTourLocationDescriptionIndex() {
        return mCursor.getColumnIndex(TourLocationContract.TourLocationEntry.QUALIFIED_COLUMN_LOCATION_DESCRIPTION);
    }

    private String getTourLocationAddress() {
        return mCursor.getString(getTourLocationAddressIndex());
    }

    private int getTourLocationAddressIndex() {
        return mCursor.getColumnIndex(TourLocationContract.TourLocationEntry.QUALIFIED_COLUMN_LOCATION_ADDRESS);
    }

    private String getTourLocationContactInfo() {
        return mCursor.getString(getTourLocationContactInfoIndex());
    }

    private int getTourLocationContactInfoIndex() {
        return mCursor.getColumnIndex(TourLocationContract.TourLocationEntry.QUALIFIED_COLUMN_LOCATION_CONTACT_INFO);
    }

    /* TourLocationResourceImage data */
    private int getResImgId() {
        int indx = getResImgIdIndex();
        return mCursor.getInt(indx);
    }

    private int getResImgIdIndex() {
        return mCursor.getColumnIndex(TourLocationContract.TourLocationResourceImage.UNIQUE_ID);
    }

    private int getResImg() {
        return mCursor.getInt(getResImgIndex());
    }

    private int getResImgIndex() {
        return mCursor.getColumnIndex(TourLocationContract.TourLocationResourceImage.QUALIFIED_COLUMN_RESOURCE_IMAGE);
    }

    /* LocationFeatureEntry data */
    private int getFeatureId() {
        int indx = getFeatureIdIndex();
        return mCursor.getInt(indx);
    }

    private int getFeatureIdIndex() {
        return mCursor.getColumnIndex(TourLocationContract.LocationFeatureEntry.UNIQUE_ID);
    }

    private String getFeatureName() {
        return mCursor.getString(getFeatureNameIndex());
    }

    private int getFeatureNameIndex() {
        return mCursor.getColumnIndex(TourLocationContract.LocationFeatureEntry.QUALIFIED_COLUMN_FEATURE_NAME);
    }

    private String getFeatureDescription() {
        return mCursor.getString(getFeatureDescriptionIndex());
    }

    private int getFeatureDescriptionIndex() {
        return mCursor.getColumnIndex(TourLocationContract.LocationFeatureEntry.QUALIFIED_COLUMN_FEATURE_DESCRIPTION);
    }

    /* LocationFeatureResourceImage data */
    private int getFeatureResImg() {
        return mCursor.getInt(getFeatureResImgIndex());
    }

    private int getFeatureResImgIndex() {
        return mCursor.getColumnIndex(TourLocationContract.LocationFeatureResourceImage.QUALIFIED_COLUMN_FEATURE_IMAGE);
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
