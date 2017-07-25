package com.meline.gentleservice.services;


import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class ComplimentWaitJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        Log.d("AppDebug", "onStartJob called");

        Bundle extras = job.getExtras();
        assert extras != null;

        int result = extras.getInt("return");


        return true; // No more work to do
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Log.d("AppDebug", "onStopJob called");
        return false; // No more work to do
    }
}