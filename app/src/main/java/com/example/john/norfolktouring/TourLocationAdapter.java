package com.example.john.norfolktouring;

import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by John on 5/17/2017.
 */

public class TourLocationAdapter extends RecyclerView.Adapter<TourLocationAdapter.TourLocationViewHolder>/*ArrayAdapter<TourLocation>*/ {
    /*** Member Variables ***/
    MainActivity mActivity;

    /**
     * The `TourLocation` objects that populate the `View`s of this `RecyclerView`.
     */
    List<TourLocation> mTourLocations;

    /**
     * The current device location.
     */
    Location mCurrentLocation;

    public TourLocationAdapter(MainActivity activity,
                               List<TourLocation> tourLocations,
                               Location deviceLocation) {
        mActivity = activity;
        mTourLocations = tourLocations;
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
        // Get the {@link TourLocation} object located at this position in the list.
        final TourLocation currentTourLocation = mTourLocations.get(position);
        // Set the image resource (select the first for these summary views).
        holder.locationImageView.setImageResource(currentTourLocation.getResourceImages().get(0));
        // Set location name.
        holder.locationNameView.setText(currentTourLocation.getLocationName());
        // Set the rating (represented as stars).
        int rating = currentTourLocation.getRating();
        for (int starIndx = 0; starIndx < 5; starIndx++) {
            ImageView starView = (ImageView) holder.ratingView.getChildAt(starIndx);
            if (starIndx < rating)
                starView.setImageResource(R.drawable.ic_star_black_24dp);
            else
                starView.setImageResource(R.drawable.ic_star_border_black_24dp);
        }
        // Set the open status (whether this location is currently open).
        Boolean locationIsOpen = currentTourLocation.getOpenNow();
        if (locationIsOpen == null)
            holder.openStatusView.setText(R.string.open_status_unavailable);
        else if (locationIsOpen)
            holder.openStatusView.setText(R.string.location_open);
        else
            holder.openStatusView.setText(R.string.location_closed);
        // Set the distance text for this location.
        final Location location = currentTourLocation.getLocation();
        // If the `Location`s for both this `TourLocation` and the device have been determined...
        if (location != null && mCurrentLocation != null) {
            // Set the appropriate text in the corresponding `View` in the list.
            String formatString = mActivity.getString(R.string.location_distance_main_view);
            holder.locationDistanceView.setText(
                    String.format(formatString, (int) mCurrentLocation.distanceTo(location)));
        }
        // Set a click listener on the Google Maps icon and text.
        holder.googleMapsView.setOnClickListener(new MapIconClickListener(mActivity, currentTourLocation));
        // Set a click listener on the Google Maps Route Plan icon and text.
        holder.googleMapsRoutePlanView.setOnClickListener(
                new DirectionsIconClickListener(mActivity, currentTourLocation, mCurrentLocation));
        // Set a click listener for a detail view of this tour location.
        TourLocationClickListener detailViewClickListener =
                new TourLocationClickListener(mActivity, currentTourLocation);
        holder.rootView.setOnClickListener(detailViewClickListener);
    }

    @Override
    public int getItemCount() {
        if (mTourLocations == null) return 0;
        return mTourLocations.size();
    }

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

        public TourLocationViewHolder(View itemView) {
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
     * Records a device location update for this `Adapter`.
     * @param location The new device location.
     */
    public void updateLocation(Location location) {
        mCurrentLocation = location;
        // Refresh the views using the new location.
        notifyDataSetChanged();
    }
}
