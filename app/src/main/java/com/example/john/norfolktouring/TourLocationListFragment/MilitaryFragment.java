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
    private static final String LOG_TAG = MilitaryFragment.class.getCanonicalName();;
    public static final String FRAGMENT_LABEL = "military_list";
    public static final String CATEGORY_LABEL = NorfolkTouring.getContext()
            .getResources().getString(R.string.military_category_label);

    /*** Methods ***/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ACTION_BAR_TITLE = CATEGORY_LABEL;
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
