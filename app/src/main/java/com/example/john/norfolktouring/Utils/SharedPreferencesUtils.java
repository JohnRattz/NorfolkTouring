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
    /*** Member Variables ***/
    private static final Context sAppContext = NorfolkTouring.getContext();
    private static final Resources sResources = sAppContext.getResources();

    /** Constants **/

    // Wifi and Cell Data
    public static final String WIFI_CELL_ENABLED_KEY =
            sResources.getString(R.string.pref_enable_wifi_cell_data_usage_key);
    public static final boolean WIFI_CELL_ENABLED_DEFAULT =
            sResources.getBoolean(R.bool.pref_enable_wifi_cell_data_usage_default);
    // Notifications
    public static final String NOTIFICATIONS_ENABLED_KEY =
            sResources.getString(R.string.pref_enable_notifications_key);
    public static final boolean NOTIFICATIONS_ENABLED_DEFAULT =
            sResources.getBoolean(R.bool.pref_enable_notifications_default);

    /*** Methods ***/

    public static SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(sAppContext);
    }

    /** Wifi and Cell Data **/

    /**
     * Returns whether or not wifi and cell data are enabled according to the SharedPreferences.
     * @param sharedPreferences The shared preferences to use. Pass null to use the default shared preferences.
     */
    public static boolean getWifiCellEnabled(SharedPreferences sharedPreferences) {
        if (sharedPreferences == null)
            sharedPreferences = getDefaultSharedPreferences();
        return sharedPreferences.getBoolean(WIFI_CELL_ENABLED_KEY, WIFI_CELL_ENABLED_DEFAULT);
    }

    /**
     * Sets whether wifi and cell data are enabled in the SharedPreferences.
     * @param sharedPreferences The shared preferences to use. Pass null to use the default shared preferences.
     */
    public static void setWifiCellEnabled(SharedPreferences sharedPreferences,
                                          boolean wifiCellEnabled) {
        if (sharedPreferences == null)
            sharedPreferences = getDefaultSharedPreferences();
        sharedPreferences.edit().putBoolean(WIFI_CELL_ENABLED_KEY, wifiCellEnabled).apply();
    }

    /** Notifications **/

    /**
     * Returns whether or not notifications are enabled according to the SharedPreferences.
     * @param sharedPreferences The shared preferences to use. Pass null to use the default shared preferences.
     */
    public static boolean getNotificationsEnabled(SharedPreferences sharedPreferences) {
        if (sharedPreferences == null)
            sharedPreferences = getDefaultSharedPreferences();
        return sharedPreferences.getBoolean(NOTIFICATIONS_ENABLED_KEY, NOTIFICATIONS_ENABLED_DEFAULT);
    }

    /**
     * Sets whether wifi and cell data are enabled in the SharedPreferences.
     * @param sharedPreferences The shared preferences to use. Pass null to use the default shared preferences.
     */
    public static void setNotificationsEnabled(SharedPreferences sharedPreferences,
                                               boolean notificationsEnabled) {
        if (sharedPreferences == null)
            sharedPreferences = getDefaultSharedPreferences();
        sharedPreferences.edit().putBoolean(NOTIFICATIONS_ENABLED_KEY, notificationsEnabled).apply();
    }

}
