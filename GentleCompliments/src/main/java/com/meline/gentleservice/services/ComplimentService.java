package com.meline.gentleservice.services;

import android.content.Intent;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.meline.gentleservice.ui.activities.ComplimentActivity;

public class ComplimentService extends JobService {
    public static final String DEFAULT_JOB_TAG = "wait_for_compliment_job";
    @Override
    public boolean onStartJob(JobParameters job) {
        //Log.d("AppDebug", "onStartJob called jobTag: "+job.getTag());
        Intent complimentingIntent = new Intent(this, ComplimentActivity.class);
        complimentingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(complimentingIntent);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Log.d("AppDebug", "onStopJob called jobTag: "+job.getTag());
        return false; // No more work to do
    }
}