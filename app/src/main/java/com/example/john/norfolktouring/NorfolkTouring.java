package com.example.john.norfolktouring;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by John on 7/3/2017.
 */

public class NorfolkTouring extends Application {
    /*** Member Variables ***/
    // TODO: Remove this.
    // A static instance of the application.
    private static NorfolkTouring sInstance;
    // The application context (not an activity context).
    private static Context sContext;

    /**
     * Constants
     **/
    public static final String YOUTUBE_DEVELOPER_KEY = "AIzaSyAqSUHLKobTJR1dYTcIDPUD8_NYbl0qc9c";
    public static final String GEO_DATA_API_KEY = "AIzaSyAWMdYhZZ8SfyveoIGgnXZBhQpM1Rcl-rA";
    // Used for URL queries with the Google Maps API.
    public static final String GEO_DATA_WEB_API_KEY = "AIzaSyAg8xr6kE81Dv2OtjoVQt6F3J33hBlcJA8";

    /*** Methods ***/

    public static NorfolkTouring getInstance() {
        return sInstance;
    }

    public static Context getContext() {
        return sContext;
    }

    /**
     * Returns a list of categories of tour locations.
     */
    public static List<String> getCategoriesList() {
        return new ArrayList<String>(Arrays.asList(
                sContext.getResources().getStringArray(R.array.categories_array)
        ));
    }

    /**
     * Returns the index corresponding to a category.
     * @return The index of the category in `R.array.categories_array` or -1 if invalid.
     */
    public static int getCategoryIndex(String category) {
        List<String> categories = getCategoriesList();
        int categoryIndx = -1;
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).equals(category)) {
                categoryIndx = i;
                break;
            }
        }
        return categoryIndx;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    /**
     * ActionBar Methods
     **/

    public static void setActionBarTitleToAppName(AppCompatActivity activity) {
        activity.getSupportActionBar().setTitle(activity.getString(R.string.app_name));
    }

    public static void setActionBarTitle(AppCompatActivity activity, String title) {
        activity.getSupportActionBar().setTitle(title);
    }

    public static String getActionBarTitle(AppCompatActivity activity) {
        return activity.getSupportActionBar().getTitle().toString();
    }

}
