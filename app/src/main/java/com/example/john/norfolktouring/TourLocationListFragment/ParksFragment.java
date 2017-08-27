package com.example.john.norfolktouring.TourLocationListFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.john.norfolktouring.NorfolkTouring;
import com.example.john.norfolktouring.R;
import com.example.john.norfolktouring.TourLocation;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by John on 5/16/2017.
 */

public class ParksFragment extends TourLocationListFragment {
    /*** Member Variables ***/
    // Constants
    private static final String LOG_TAG = ParksFragment.class.getCanonicalName();;
    public static final String FRAGMENT_LABEL = "parks_list";
    public static final String CATEGORY_LABEL = NorfolkTouring.getContext()
            .getResources().getString(R.string.parks_category_label);

    /*** Methods ***/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ACTION_BAR_TITLE = CATEGORY_LABEL;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void initLocations() {
        super.initLocations(CATEGORY_LABEL);
        if (mLocations == null) {
            // Create a list of locations.
            final int numLocations = 4;
            mLocations = new ArrayList<>(numLocations);

            // Town Point Park
            mLocations.add(new TourLocation("Town Point Park",
                    getResources().getString(R.string.parks_town_point_park_desc),
                    new ArrayList<Integer>(Arrays.asList(
                            R.drawable.parks_town_point_park1,
                            R.drawable.parks_town_point_park2
                    )),
                    "Waterside Drive, Norfolk, VA 23510",
                    "(757) 664-6880",
                    new ArrayList<TourLocation.LocationFeature>(Arrays.asList(
                            new TourLocation.LocationFeature(
                                    "Public Parking Lots", null, null
                            ),
                            new TourLocation.LocationFeature(
                                    "Stage / Auditorium", null, null
                            ),
                            new TourLocation.LocationFeature(
                                    "Tables", null, null
                            ),
                            new TourLocation.LocationFeature(
                                    "Trash Cans", null, null
                            ),
                            new TourLocation.LocationFeature(
                                    "Water Splash Pad", null, null
                            ),
                            new TourLocation.LocationFeature(
                                    "Waterfront", null, null
                            )))));

            // Ocean View Beach Park
            mLocations.add(new TourLocation("Ocean View Beach Park",
                    getResources().getString(R.string.parks_ocean_view_beach_park_desc),
                    new ArrayList<Integer>(Arrays.asList(
                            R.drawable.parks_ocean_view_beach_park1,
                            R.drawable.parks_ocean_view_beach_park2,
                            R.drawable.parks_ocean_view_beach_park3
                    )),
                    "100 W. Ocean View Ave., Norfolk, VA 23503",
                    null,
                    null));

            // Community Beach Park
            mLocations.add(new TourLocation("Community Beach Park",
                    null,
                    new ArrayList<Integer>(Arrays.asList(
                            R.drawable.parks_community_beach_park1,
                            R.drawable.parks_community_beach_park2,
                            R.drawable.parks_community_beach_park3
                    )),
                    "700 E. Ocean View Ave, Norfolk, VA 23503",
                    null,
                    null));

            // Sarah Constant Beach Park
            mLocations.add(new TourLocation("Sarah Constant Beach Park",
                    null,
                    new ArrayList<Integer>(Arrays.asList(
                            R.drawable.parks_sarah_constant_beach_park1,
                            R.drawable.parks_sarah_constant_beach_park2,
                            R.drawable.parks_sarah_constant_beach_park3
                    )),
                    "300 W. Ocean View Avenue, Norfolk, VA 23510",
                    null,
                    null));
            // Record these TourLocations statically.
            TourLocation.addTourLocations(CATEGORY_LABEL, mLocations);
        }
    }
}
