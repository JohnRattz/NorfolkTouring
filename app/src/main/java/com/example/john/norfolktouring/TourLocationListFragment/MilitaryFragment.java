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

public class MilitaryFragment extends TourLocationListFragment {
    /*** Member Variables ***/
    // Constants
    static {LOG_TAG = MilitaryFragment.class.getCanonicalName();}

    /*** Methods ***/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ACTION_BAR_TITLE = NorfolkTouring.getContext().getResources()
                        .getString(R.string.military_category_label);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected void createLocations() {
        // Create a list of locations.
        final int numLocations = 2;
        mLocations = new ArrayList<>(numLocations);

        // Add the locations.
        // Nauticus
        mLocations.add(new TourLocation("Nauticus",
                getResources().getString(R.string.military_nauticus_desc),
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.military_nauticus1,
                        R.drawable.military_nauticus2
                )),
                "1 Waterside Dr, Norfolk, VA 23510",
                "(757) 664-1000",
                new ArrayList<TourLocation.LocationFeature>(Arrays.asList(
                        new TourLocation.LocationFeature(
                                "USS Wisconsin (BB-64)",
                                getString(R.string.military_nauticus_uss_wisconsin),
                                new ArrayList<Integer>(Arrays.asList(
                                        R.drawable.military_battleship_wisconsin1,
                                        R.drawable.military_battleship_wisconsin2
                                ))
                        ),
                        new TourLocation.LocationFeature(
                                "Hampton Roads Naval Museum (2nd Floor)",
                                getString(R.string.military_nauticus_hampton_roads_naval_museum),
                                new ArrayList<Integer>(Arrays.asList(
                                        R.drawable.military_hampton_roads_naval_museum1,
                                        R.drawable.military_hampton_roads_naval_museum2
                                ))
                        ),
                        new TourLocation.LocationFeature(
                                "Power of the Sea Galleries (3rd Floor)",
                                getString(R.string.military_nauticus_power_of_the_sea_galleries),
                                new ArrayList<Integer>(Arrays.asList(
                                        R.drawable.military_nauticus_power_of_the_sea1,
                                        R.drawable.military_nauticus_power_of_the_sea2,
                                        R.drawable.military_nauticus_power_of_the_sea3,
                                        R.drawable.military_nauticus_power_of_the_sea4,
                                        R.drawable.military_nauticus_power_of_the_sea5
                                ))
                        ),
                        new TourLocation.LocationFeature(
                                "Wisconsin Vista",
                                getString(R.string.military_nauticus_wisconsin_vista),
                                new ArrayList<Integer>(Arrays.asList(
                                        R.drawable.military_nauticus_wisconsin_vista1
                                ))
                        ),
                        new TourLocation.LocationFeature(
                                "Living Sea Landing",
                                getString(R.string.military_nauticus_living_sea_landing),
                                new ArrayList<Integer>(Arrays.asList(
                                        R.drawable.military_nauticus_living_sea_landing1,
                                        R.drawable.military_nauticus_living_sea_landing2,
                                        R.drawable.military_nauticus_living_sea_landing3
                                        ))
                        ),
                        new TourLocation.LocationFeature(
                                "Living Sea Theater",
                                getString(R.string.military_nauticus_living_sea_theater),
                                new ArrayList<Integer>(Arrays.asList(
                                        R.drawable.military_nauticus_living_sea_theater1,
                                        R.drawable.military_nauticus_living_sea_theater2
                                ))
                        ),
                        new TourLocation.LocationFeature(
                                "Lighthouse Portico",
                                getString(R.string.military_nauticus_lighthouse_portico),
                                new ArrayList<Integer>(Arrays.asList(
                                        R.drawable.military_nauticus_lighthouse_portico1,
                                        R.drawable.military_nauticus_lighthouse_portico2
                                ))
                        ),
                        new TourLocation.LocationFeature(
                                "Elizabeth River Pavilion",
                                getString(R.string.military_nauticus_elizabeth_river_pavilion),
                                new ArrayList<Integer>(Arrays.asList(
                                        R.drawable.military_nauticus_elizabeth_river_pavilion1
                                ))
                        )))));

        // MacArthur Memorial
        mLocations.add(new TourLocation("MacArthur Memorial",
                getResources().getString(R.string.military_macarthur_memorial_desc),
                new ArrayList<Integer>(Arrays.asList(
                           R.drawable.military_macarthur_memorial1,
                           R.drawable.military_macarthur_memorial2,
                           R.drawable.military_macarthur_memorial3,
                           R.drawable.military_macarthur_memorial4
                )),
                "198 Bank St, Norfolk, VA 23510",
                "(757) 441-2965",
                null));
    }
}
