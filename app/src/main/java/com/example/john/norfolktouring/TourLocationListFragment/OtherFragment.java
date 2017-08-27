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

public class OtherFragment extends TourLocationListFragment {
    /*** Member Variables ***/
    // Constants
    private static final String LOG_TAG = OtherFragment.class.getCanonicalName();;
    public static String getFragmentLabel() {return "other_list";}

    /*** Methods ***/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ACTION_BAR_TITLE = NorfolkTouring.getContext().getResources()
                .getString(R.string.others_category_label);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected ArrayList<TourLocation> createLocations() {
        // Create a list of locations.
        final int numLocations = 3;
        ArrayList<TourLocation> locations = new ArrayList<>(numLocations);

        // Norfolk Botanical Garden
        locations.add(new TourLocation("Norfolk Botanical Garden",
                getResources().getString(R.string.other_norfolk_botanical_garden_desc),
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.other_norfolk_botanical_garden1,
                        R.drawable.other_norfolk_botanical_garden2,
                        R.drawable.other_norfolk_botanical_garden3,
                        R.drawable.other_norfolk_botanical_garden4
                )),
                "6700 Azalea Garden Rd, Norfolk, VA 23518",
                "(757) 441-5830",
                null));

        // The Virginia Zoo
        locations.add(new TourLocation("The Virginia Zoo",
                getResources().getString(R.string.other_virginia_zoo_desc),
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.other_virginia_zoo1,
                        R.drawable.other_virginia_zoo2,
                        R.drawable.other_virginia_zoo3,
                        R.drawable.other_virginia_zoo4
                )),
                "3500 Granby St, Norfolk, VA 23504",
                "(757) 441-2374",
                null));

        // American Rover
        locations.add(new TourLocation("American Rover",
                getResources().getString(R.string.other_american_rover_desc),
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.other_american_rover1,
                        R.drawable.other_american_rover2,
                        R.drawable.other_american_rover3,
                        R.drawable.other_american_rover4
                )),
                "333 Waterside Dr, Norfolk, VA 23510",
                "(757) 627-7245",
                null));
        return locations;
    }
}
