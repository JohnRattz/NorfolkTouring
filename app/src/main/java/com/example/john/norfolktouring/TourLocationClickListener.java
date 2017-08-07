package com.example.john.norfolktouring;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;


/**
 * Created by John on 6/1/2017.
 */

public class TourLocationClickListener implements View.OnClickListener{
    private MainActivity mActivity;
    private TourLocation mTourLocation;

    public TourLocationClickListener(MainActivity activity, TourLocation tourLocation) {
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
