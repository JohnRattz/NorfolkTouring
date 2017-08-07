package com.example.john.norfolktouring;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.john.norfolktouring.TourLocationListFragment.MilitaryFragment;
import com.example.john.norfolktouring.TourLocationListFragment.MuseumsFragment;
import com.example.john.norfolktouring.TourLocationListFragment.OtherFragment;
import com.example.john.norfolktouring.TourLocationListFragment.ParksFragment;
import com.example.john.norfolktouring.TourLocationListFragment.RestaurantsFragment;
import com.example.john.norfolktouring.TourLocationListFragment.TourLocationListFragment;

import java.util.ArrayList;
import java.util.List;

import static com.example.john.norfolktouring.NorfolkTouring.setActionBarTitle;

/**
 * Created by John on 5/16/2017.
 */

class DrawerItemClickListener implements ListView.OnItemClickListener {
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private MainActivity mActivity;
    private List<String> mCategories;
    private ActionBar mActionBar;

    DrawerItemClickListener(MainActivity activity, ListView drawerList,
                            List<String> categories, ActionBar actionBar){
        this.mActivity = activity;
        this.mDrawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);
        this.mDrawerList = drawerList;
        this.mCategories = categories;
        this.mActionBar = actionBar;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
    }

    /**
     * Swaps fragments in the main content view
     */
    public void selectItem(int position) {
        Fragment fragment = null;
        String category = mCategories.get(position);
        // Note that these must be in the same order as listed in @arrays/categories_array.
        switch (category){
            case "Parks":
                fragment = new ParksFragment();
                break;
            case "Museums":
                fragment = new MuseumsFragment();
                break;
            case "Military":
                fragment = new MilitaryFragment();
                break;
            case "Restaurants":
                fragment = new RestaurantsFragment();
                break;
            case "Other":
                fragment = new OtherFragment();
                break;
        }

        // Insert the fragment by replacing any existing fragment.
        FragmentManager fragmentManager = mActivity.getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment, TourLocationListFragment.FRAGMENT_LABEL)
                .addToBackStack(TourLocationListFragment.FRAGMENT_LABEL)
                .commit();

        // Highlight the selected item, update the title, and close the drawer.
        mDrawerList.setItemChecked(position, true);
//        setActionBarTitle(mActivity, category);
        mDrawerLayout.closeDrawer(mDrawerList);
    }
}