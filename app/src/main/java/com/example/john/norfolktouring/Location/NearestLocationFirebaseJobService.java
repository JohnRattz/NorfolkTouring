package com.example.john.norfolktouring.Location;

import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by John on 8/27/2017.
 */

public class NearestLocationFirebaseJobService extends JobService {

    private AsyncTask mBackgroundTask;

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        //  This runs on the main thread, so run an AsyncTask
        mBackgroundTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                // Issue the nearest location notification.
                NearestLocationTasks.executeTask(
                        NearestLocationFirebaseJobService.this,
                        NearestLocationTasks.ACTION_CLOSEST_LOCATION_NOTIFICATION);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                // Inform the JobManager that the job has completed
                // and that the job should be rescheduled.
                jobFinished(jobParameters, true);
            }
        };
        mBackgroundTask.execute();
        // The AsyncTask is still running, so work is still being done.
        return true;
    }

    /**
     * Called when this job is stopped - not when it has completed.
     * @return Whether or not the job should be retried.
     */
    @Override
    public boolean onStopJob(JobParameters job) {
        // If there is a background task, cancel it.
        if (mBackgroundTask != null) mBackgroundTask.cancel(true);
        return true;
    }
}
