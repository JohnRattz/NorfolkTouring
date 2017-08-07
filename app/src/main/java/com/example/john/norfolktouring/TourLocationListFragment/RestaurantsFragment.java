package com.example.john.norfolktouring.TourLocationListFragment;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.john.norfolktouring.NorfolkTouring;
import com.example.john.norfolktouring.R;
import com.example.john.norfolktouring.TourLocation;

import java.util.ArrayList;
import java.util.Arrays;

import static com.example.john.norfolktouring.NorfolkTouring.setActionBarTitle;

/**
 * Created by John on 5/16/2017.
 */

public class RestaurantsFragment extends TourLocationListFragment {
    /*** Member Variables ***/
    // Constants
    static {LOG_TAG = RestaurantsFragment.class.getCanonicalName();}
    static {ACTION_BAR_TITLE =
            NorfolkTouring.getContext().getResources().getString(R.string.restaurants_category_label);}

    /*** Methods ***/

    protected void createLocations() {
        // Create a list of locations.
        final int numLocations = 6;
        mLocations = new ArrayList<>(numLocations);

        // Franco's
        mLocations.add(new TourLocation("Franco's",
                getResources().getString(R.string.restaurants_francos_desc),
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.restaurants_francos1
                )),
                "6200 N Military Hwy, Norfolk, VA 23518",
                "(757) 853-0177",
                null));

        // Azalea Inn & Time Out Sports Bar
        mLocations.add(new TourLocation("Azalea Inn & Time Out Sports Bar",
                getResources().getString(
                        R.string.restaurants_azalea_inn_desc),
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.restaurants_azalea_inn1
                )),
                "2344 E Little Creek Rd, Norfolk, VA 23518",
                "(757) 587-4649",
                null));

        // Paradise Pizzeria
        mLocations.add(new TourLocation("Paradise Pizzeria",
                getResources().getString(
                        R.string.restaurants_paradise_pizzera_desc),
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.restaurants_paradise_pizzera1
                )),
                "6144 Chesapeake Blvd, Norfolk, VA 23513",
                "(757) 855-0100",
                null));

        // Regino's Italian Restaurant
        mLocations.add(new TourLocation("Regino's Italian Restaurant",
                getResources().getString(
                        R.string.restaurants_reginos_desc),
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.restaurants_reginos1
                )),
                "3816 E Little Creek Rd, Norfolk, VA 23518",
                "(757) 588-4300",
                null));

        // Doumar's Cones & Barbecue
        mLocations.add(new TourLocation("Doumar's Cones & Barbecue",
                getResources().getString(
                        R.string.restaurants_doumars_desc),
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.restaurants_doumars1,
                        R.drawable.restaurants_doumars2,
                        R.drawable.restaurants_doumars3,
                        R.drawable.restaurants_doumars4,
                        R.drawable.restaurants_doumars5
                )),
                "1919 Monticello Ave, Norfolk, VA 23517",
                "(757) 627-4163",
                null));

        // Handsome Biscuit
        mLocations.add(new TourLocation("Handsome Biscuit",
                getResources().getString(
                        R.string.restaurants_handsome_biscuit_desc),
                new ArrayList<Integer>(Arrays.asList(
                        R.drawable.restaurants_handsome_biscuit1,
                        R.drawable.restaurants_handsome_biscuit2,
                        R.drawable.restaurants_handsome_biscuit3,
                        R.drawable.restaurants_handsome_biscuit4,
                        R.drawable.restaurants_handsome_biscuit5,
                        R.drawable.restaurants_handsome_biscuit6
                )),
                "2511 Colonial Ave, Norfolk, VA 23517",
                null,
                null));
    }
}
