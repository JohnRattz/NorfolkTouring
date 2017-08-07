package com.example.john.norfolktouring.TourLocationListFragment;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
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
import com.example.john.norfolktouring.TourLocationDetailFragment;
import com.example.john.norfolktouring.Utils.PlacesUtils;
import com.example.john.norfolktouring.R;
import com.example.john.norfolktouring.TourLocation;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

import static com.example.john.norfolktouring.Constants.SAVED_STATE;
import static com.example.john.norfolktouring.NorfolkTouring.setActionBarTitle;

/**
 * Created by John on 7/3/2017.
 */

/**
 * A general formulation of a Fragment that displays `TourLocation` objects.
 */
public abstract class TourLocationListFragment extends Fragment {
    /*** Member Variables ***/
    protected MainActivity mActivity;
    protected ArrayList<TourLocation> mLocations;
    private TourLocationAdapter mAdapter;
    // Used to save and restore instance state.
    // Primarily used to save state when the fragment is put on the back stack.
    protected Bundle savedState;

    // Constants
    // Strings for storing state information.
    private static final String LOCATIONS = "mLocations";

    protected static String LOG_TAG;
    static {LOG_TAG = TourLocationListFragment.class.getCanonicalName();}

    public static final String FRAGMENT_LABEL = "list";

    // This should always be a category label from strings.xml.
    static String ACTION_BAR_TITLE = "";

    /*** Methods ***/

    /**
     * Creates the `TourLocation`s to fill the view (via the adapter `mAdapter`).
     * Must fill `mLocations`.
     */
    protected abstract void createLocations();

    /** Lifecycle Methods **/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (MainActivity) getActivity();

        // Set the Action Bar title to the name of this category.
        setActionBarTitle(mActivity, ACTION_BAR_TITLE);

        // Record that this Fragment is the currently displayed one in `MainActivity`.
        mActivity.setCurrentFragment(this);

        View rootView = inflater.inflate(R.layout.location_list, container, false);

        // If the state was saved normally (e.g. activity was paused), restore normally.
        // Otherwise (likely now being restored from the back stack), restore from `savedState`.
        if (savedInstanceState != null && savedState == null) {
            savedState = savedInstanceState.getBundle(SAVED_STATE);
        }
        if (savedState != null) {
            mLocations = savedState.getParcelableArrayList(LOCATIONS);
        } else {
            // Create a list of locations.
            createLocations();
        }
        savedState = null;

        // Create an adapter for the locations.
        mAdapter = new TourLocationAdapter((MainActivity) getActivity(), mLocations, mActivity.getCurrentLocation());
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.location_list);
        recyclerView.setAdapter(mAdapter);
        // Add a divider between items (like `ListView` default).
        LinearLayoutManager recyclerViewLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(),
                        recyclerViewLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Get the `Location`, hours of operation, rating, and website for these places.
        PlacesUtils.getInfoForTourLocations(mActivity, mLocations, mAdapter);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Accommodates this fragment being added to the back stack.
        savedState = saveState();
    }

    protected Bundle saveState() {
        Bundle state = new Bundle();
        state.putParcelableArrayList(LOCATIONS, mLocations);
        return state;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBundle(SAVED_STATE, (savedState != null) ? savedState : saveState());
    }

    /** Location Updates Methods **/

    public void locationCallback(Location location) {
        // Update the list items with the current distance to them.
        mAdapter.updateLocation(location);
    }

    /**
     * Populates a TourLocationListFragment with views populated with TourLocation information.
     */
    public static class TourLocationAdapter extends RecyclerView.Adapter<TourLocationAdapter.TourLocationViewHolder>/*ArrayAdapter<TourLocation>*/ {
        /*** Member Variables ***/
        private MainActivity mActivity;

        /**
         * The `TourLocation` objects that populate the `View`s of this `RecyclerView`.
         */
        private List<TourLocation> mTourLocations;

        /**
         * The current device location.
         */
        private Location mCurrentLocation;

        TourLocationAdapter(MainActivity activity,
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
         * Records a device location update for this `Adapter`.
         * @param location The new device location.
         */
        void updateLocation(Location location) {
            mCurrentLocation = location;
            // Refresh the views using the new location.
            notifyDataSetChanged();
        }

        /**
         * Class for the `RecyclerView` items.
         */
        static class TourLocationViewHolder extends RecyclerView.ViewHolder {
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
        private static class TourLocationClickListener implements View.OnClickListener{
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
                bundle.putParcelable("location", mTourLocation/*location*/);
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
}
