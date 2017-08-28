package com.example.john.norfolktouring.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.example.john.norfolktouring.NorfolkTouring;
import com.example.john.norfolktouring.R;

/**
 * Created by John on 8/27/2017.
 */

public class SharedPreferencesUtils {
    private static final Context sAppContext = NorfolkTouring.getContext();
    private static final Resources sResources = sAppContext.getResources();

    // Wifi and cell data
    public static final String WIFI_CELL_ENABLED_SHARED_PREFERENCE_KEY =
            sResources.getString(R.string.pref_enable_wifi_cell_data_usage_key);
    public static final boolean WIFI_CELL_ENABLED_SHARED_PREFERENCE_DEFAULT =
            sResources.getBoolean(R.bool.pref_enable_wifi_cell_data_usage_default);

    /*** Methods ***/

    public static SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(sAppContext);
    }

    /**
     * Returns whether or not wifi and cell data are enabled according to the SharedPreferences.
     * @param sharedPreferences The shared preferences to use. Pass null to use the default shared preferences.
     */
    public static boolean getWifiCellEnabledSharedPreference(SharedPreferences sharedPreferences) {
        if (sharedPreferences == null)
            sharedPreferences = getDefaultSharedPreferences();
        String key = WIFI_CELL_ENABLED_SHARED_PREFERENCE_KEY;
        boolean defaultValue = WIFI_CELL_ENABLED_SHARED_PREFERENCE_DEFAULT;
        return sharedPreferences.getBoolean(key, defaultValue);
    }
}
