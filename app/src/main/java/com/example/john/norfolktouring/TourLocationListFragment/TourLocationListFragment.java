package com.example.john.norfolktouring.TourLocationListFragment;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.john.norfolktouring.MainActivity;
import com.example.john.norfolktouring.Utils.PlacesUtils;
import com.example.john.norfolktouring.R;
import com.example.john.norfolktouring.TourLocation;
import com.example.john.norfolktouring.TourLocationAdapter;
import com.example.john.norfolktouring.TourLocationClickListener;
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
    private GoogleApiClient mGoogleApiClient;
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

        // This fragment is only ever created by `MainActivity`
        // after a `GoogleApiClient` has been created.
        mGoogleApiClient = mActivity.getGoogleApiClient();

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
}
