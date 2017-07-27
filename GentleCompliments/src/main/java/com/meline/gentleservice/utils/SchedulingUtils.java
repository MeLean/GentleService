package com.meline.gentleservice.utils;

import android.content.Context;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.meline.gentleservice.ProjectConstants;
import com.meline.gentleservice.R;
import com.meline.gentleservice.services.ComplimentService;

import java.util.Date;
import java.util.Random;

public class SchedulingUtils {
    private static final int MILLISECONDS_TO_MINUTES_CONSTANT = 60000;
    private static int errorsCount = 0;

     public static void startComplimentingJob(Context context, int triggerAt, int continues) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
            Job surpriseJob = dispatcher.newJobBuilder()
                    .setService(ComplimentService.class)
                    .setRecurring(false)
                    .setReplaceCurrent(true)
                    .setTrigger(Trigger.executionWindow(triggerAt, continues))
                    .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                    .setTag(ComplimentService.DEFAULT_JOB_TAG)
                    .setLifetime(Lifetime.FOREVER)
                    .build();
         final int result = dispatcher.schedule(surpriseJob);

         if (result != FirebaseJobDispatcher.SCHEDULE_RESULT_SUCCESS) {
             Log.d("AppDebug", "SchedulingUtils errorCount: " + errorsCount);
             if(++errorsCount < 100){
                 startComplimentingJob(context, triggerAt, continues);
             }
         }else{
             Log.d("AppDebug", "SchedulingUtils started a job");
             errorsCount = 0;
         }
    }

    public static int calculateWaitingTimeInSeconds(Context context, boolean isSurpriseMe, boolean isScheduled) {
        int DEFAULT_VALUE = 2; //todo 8*60*60; use 8 hours as default
        if (isSurpriseMe) {
            long nextLaunch = SharedPreferencesUtils.loadLong(context, context.getString(R.string.sp_next_surprise_milliseconds), System.currentTimeMillis());

            long millisecondsToWait = nextLaunch - System.currentTimeMillis();
            int secondsToWait = millisecondsToWait >0 ? (int) millisecondsToWait/1000 : 0;
            int randNum = generateRandom(secondsToWait);
            Log.d("AppDebug", "ComplimentActivity calculateWaitingTimeInSeconds randNum: "+randNum
                    +"\nnextLaunch date: "+ new Date(nextLaunch)
                    +"\nCur Date"+ new Date(System.currentTimeMillis())
                    +"\nsecondsToWait: "+secondsToWait
                    +"\nmillisecondsToWait:" + millisecondsToWait
            );
            return randNum;
        }

        if (isScheduled) {
            String savedStringValue = SharedPreferencesUtils.loadString(context, context.getString(R.string.sp_time_wait_value), null);
            Log.d("AppDebug", "savedStringValue: " + savedStringValue);
            //return saved time in minutes
            return savedStringValue != null ? Integer.parseInt(savedStringValue) * 60 : DEFAULT_VALUE;
        }

        return DEFAULT_VALUE;
    }


    public static String checkForErrors(Context context, String enteredInt) {
        long MINIMUM_WAITING_TIME = 1; //todo  2 * 60 * 60; two hours in seconds is a minimum time
        try {
            //make time in seconds, because FirebaseJobDispatcher works in seconds
            int inputNum = (Integer.parseInt(String.valueOf(enteredInt))) * 60;

            if (inputNum < MINIMUM_WAITING_TIME) {
                if (inputNum < 0) {
                    throw new NumberFormatException("Just catch me to return error message!");
                }

                //make saved MINIMUM_WAITING_TIME in minutes
                //to be more understandable for the users
                return context.getString(R.string.minimum_waiting_time_text) + MINIMUM_WAITING_TIME / 60;
            }
        } catch (NumberFormatException e) {
            return context.getString(R.string.invalid_number_text);
        }

        return null;
    }

    public static int generateRandom(int num) {
        Random random = new Random();
        return random.nextInt(num);
    }
}
