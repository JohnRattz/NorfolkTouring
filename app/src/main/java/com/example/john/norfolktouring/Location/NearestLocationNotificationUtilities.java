package com.example.john.norfolktouring.Location;

import android.content.Context;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

/**
 * Created by John on 8/27/2017.
 */

public class NearestLocationNotificationUtilities {
    // 5 minutes between reminders
    private static final int REMINDER_INTERVAL_SECONDS = 5 * 60;
    // 5 minutes of flex time (window of time for notification after REMINDER_INTERVAL_SECONDS)
    private static final int SYNC_FLEXTIME_SECONDS = 5 * 60;
    private static final String NEAREST_LOCATION_JOB_TAG = "nearest_location_tag";
    private static boolean sInitialized;

    /**
     * Schedules a nearest location notification roughly every REMINDER_INTERVAL_SECONDS.
     */
    synchronized public static void scheduleNearestLocationNotifications(Context context) {
        // If the job has already been initialized, return
        if (sInitialized) return;
        GooglePlayDriver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(driver);
        Job scheduleChargingReminderJob = jobDispatcher.newJobBuilder()
                .setService(NearestLocationFirebaseJobService.class)
                /* A unique tag used to identify this job */
                .setTag(NEAREST_LOCATION_JOB_TAG)
                /* Only run while charging */
                .setConstraints(Constraint.ON_ANY_NETWORK)
                /* Job runs forever */
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        REMINDER_INTERVAL_SECONDS,
                        REMINDER_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                /* Replace any currently running instance of this job */
                .setReplaceCurrent(true)
                .build();
        jobDispatcher.schedule(scheduleChargingReminderJob);
        // Note that we're done starting the job.
        sInitialized = true;
    }
}
