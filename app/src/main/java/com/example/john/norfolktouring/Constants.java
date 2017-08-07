package com.example.john.norfolktouring;

import android.content.res.Resources;

/**
 * Created by John on 6/26/2017.
 */

public class Constants {
    // Strings for storing state information.
    public static final String SAVED_STATE = "SAVED_STATE";

    /**
     * Courtesy of
     * <a href="https://stackoverflow.com/a/19953871/5449970">this Stack Overflow post</a>.
     * @param dp The number of dp to be converted to px.
     */
    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * Courtesy of
     * <a href="https://stackoverflow.com/a/19953871/5449970">this Stack Overflow post</a>.
     * @param px The number of px to be converted to dp.
     */
    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}
