package com.example.john.norfolktouring.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.example.john.norfolktouring.Location.NearestLocationIntentService;
import com.example.john.norfolktouring.MainActivity;
import com.example.john.norfolktouring.NorfolkTouring;
import com.example.john.norfolktouring.R;

/**
 * Created by John on 8/27/2017.
 */

public class NotificationUtils {

    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    /**
     * Create a notification for the nearest location.
     * @param category The name of the category of TourLocation that contains the closest location.
     * @param distance The distance to the closest location
     */
    public static void notifyOfNearestLocation(Context context, String locationName,
                                               String category, int distance) {
        Resources resources = context.getResources();
        // TODO: Format the title text and body text.
        String titleTextFormat = resources.getString(R.string.nearest_location_notification_title);
        String formattedTitle = String.format(titleTextFormat, locationName);
        String bodyTextFormat = resources.getString(R.string.nearest_location_notification_body);
        String formattedBody = String.format(bodyTextFormat, distance);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setColor(resources.getColor(R.color.colorPrimary))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(largeIcon(context))
                .setContentTitle(formattedTitle)
                .setContentText(formattedBody)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bodyTextFormat))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(nearestLocationContentIntent(context, category))
                .setAutoCancel(true);
        // If the build version is greater than JELLY_BEAN, set the notification's priority to PRIORITY_HIGH.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(
                NearestLocationIntentService.CLOSEST_LOCATION_NOTIFICATION_ID,
                notificationBuilder.build());
    }

    /**
     * Creates the intent that is issued when the nearest location notification is clicked on.
     */
    public static PendingIntent nearestLocationContentIntent(Context context, String category) {
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        // Have MainActivity open and load the TourLocationListFragment for the nearest location.
        int categoryIndx = NorfolkTouring.getCategoryIndex(category);
        Bundle extras = new Bundle();
        extras.putInt(MainActivity.EXTRA_CATEGORY_INDX, categoryIndx);
        mainActivityIntent.putExtras(extras);
        return PendingIntent.getActivity(
                context, NearestLocationIntentService.CLOSEST_LOCATION_NOTIFICATION_PENDING_INTENT,
                mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Retrieves an icon for notifications.
     */
    public static Bitmap largeIcon(Context context) {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_raw_24dp);
    }
}
