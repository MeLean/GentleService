package com.meline.gentleservice.utils;

import android.app.Activity;
import android.content.Context;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.meline.gentleservice.services.ComplimentService;

public class DispatcherUtils {
     public static void startComplimentingJob(Context context, int triggerAt, int continues) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        Job surpriseJob = dispatcher.newJobBuilder()
                .setService(ComplimentService.class)
                .setRecurring(false)
                .setReplaceCurrent(true)
                .setTrigger(Trigger.executionWindow(triggerAt, continues))
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                .setTag(ComplimentService.DEFAULT_JOB_TAG)
                .setLifetime(Lifetime.FOREVER)
                .build();
        dispatcher.mustSchedule(surpriseJob);
    }
}
