package com.example.john.norfolktouring.Location;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NearestLocationIntentService extends IntentService {
    private static final String ACTION_CLOSEST_LOCATION_NOTIFICATION =
            "com.example.john.norfolktouring.action.CLOSEST_LOCATION_NOTIFICATION";

    // TODO: Add constant Strings for Intent extras if needed.

    public NearestLocationIntentService() {
        super("NearestLocationIntentService");
    }

    /**
     * Initiates regular notifications indicating the closest location and the distance to it.
     */
    // TODO: "Notification" -> "Notifications" ?.
    public static void startClosestLocationNotification(Context context) {
        Intent intent = new Intent(context, NearestLocationIntentService.class);
        intent.setAction(ACTION_CLOSEST_LOCATION_NOTIFICATION);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null) {
                switch(action) {
                    case ACTION_CLOSEST_LOCATION_NOTIFICATION:
                        closestLocationNotification();
                        break;
                }
            }
        }
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    // TODO: Finish this.
    private void closestLocationNotification() {
        // TODO: Get the closest location.
        // TODO: Determine the distance to it.
        // TODO: Create and show a notification.

//        throw new UnsupportedOperationException("Not yet implemented");
    }
}
