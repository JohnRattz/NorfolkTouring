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

public class MuseumsFragment extends TourLocationListFragment {
    /*** Member Variables ***/
    // Constants
    static {LOG_TAG = MuseumsFragment.class.getCanonicalName();}

    /*** Methods ***/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ACTION_BAR_TITLE = NorfolkTouring.getContext().getResources()
                .getString(R.string.museums_category_label);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected void createLocations() {
        // Create a list of locations.
        final int numLocations = 3;
        mLocations = new ArrayList<>(numLocations);

        // Chrysler Museum of Art.
        mLocations.add(new TourLocation("Chrysler Museum of Art",
                getResources().getString(R.string.museum_chrysler_museum_desc),
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.museums_chrysler_museum_of_art1,
                        R.drawable.museums_chrysler_museum_of_art2,
                        R.drawable.museums_chrysler_museum_of_art3,
                        R.drawable.museums_chrysler_museum_of_art4,
                        R.drawable.museums_chrysler_museum_of_art5,
                        R.drawable.museums_chrysler_museum_of_art6,
                        R.drawable.museums_chrysler_museum_of_art7,
                        R.drawable.museums_chrysler_museum_of_art8
                )),
                "1 Memorial Pl, Norfolk, VA 23510",
                "(757) 664-6200",
                null));

        // Hampton Roads Naval Museum
        mLocations.add(new TourLocation("Hampton Roads Naval Museum",
                getResources().getString(R.string.museum_hampton_roads_naval_museum_desc),
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.military_hampton_roads_naval_museum1,
                        R.drawable.military_hampton_roads_naval_museum2
                )),
                "1 Waterside Dr, Norfolk, VA 23510",
                "(757) 322-2987",
                null));

        // Hermitage Museum & Gardens
        mLocations.add(new TourLocation("Hermitage Museum & Gardens",
                getResources().getString(R.string.museum_hermitage_museum_and_gardens_desc),
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.museums_hermitage_museum_and_gardens1,
                        R.drawable.museums_hermitage_museum_and_gardens1
                )),
                "7637 N Shore Rd, Norfolk, VA 23505",
                "(757) 423-2052",
                null));
    }
}
