package com.example.john.norfolktouring.TourLocationListFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.john.norfolktouring.NorfolkTouring;
import com.example.john.norfolktouring.R;

/**
 * Created by John on 5/16/2017.
 */

public class OtherFragment extends TourLocationListFragment {
    /*** Member Variables ***/
    // Constants
    private static final String LOG_TAG = OtherFragment.class.getCanonicalName();;
    public static final String FRAGMENT_LABEL = "other_list";
    public static final String CATEGORY_LABEL = NorfolkTouring.getContext()
            .getResources().getString(R.string.others_category_label);

    /*** Methods ***/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        CURRENT_CATEGORY_LABEL = CATEGORY_LABEL;
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
