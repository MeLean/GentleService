package com.meline.gentleservice.utils;

import android.content.Context;
import android.util.Log;

import com.meline.gentleservice.ProjectConstants;
import com.meline.gentleservice.R;
import com.meline.gentleservice.services.AlarmsProvider;

import java.util.Date;
import java.util.Random;

public class SchedulingUtils {
    public static void startComplimentingJob(Context context) {
        AlarmsProvider alarmsProvider = new AlarmsProvider();
        boolean isSurpriseMe = SharedPreferencesUtils.loadBoolean(context, context.getString(R.string.sp_surprise_me), true);
        boolean isScheduled = SharedPreferencesUtils.loadBoolean(context, context.getString(R.string.sp_is_scheduled), false);
        long calculatedTriggerAtMilliseconds = calculateTriggerAtMilliseconds(context, isSurpriseMe, isScheduled);
        saveNextTriggerDate(context, calculatedTriggerAtMilliseconds);
        alarmsProvider.setAlarm(context, calculatedTriggerAtMilliseconds);
    }

    private static long calculateTriggerAtMilliseconds(Context context, boolean isSurpriseMe, boolean isScheduled) {
        int DEFAULT_VALUE_IN_MILLISECONDS = 120000; //todo 8*60*60*1000; use 8 hours as default
        if (isSurpriseMe) {

            int surprisePeriod = SharedPreferencesUtils.loadInt(
                    context,
                    context.getString(R.string.sp_surprise_time_max_value),
                    DEFAULT_VALUE_IN_MILLISECONDS
            );
            long currentMilliseconds = System.currentTimeMillis();
            long surpriseEndingMilliseconds = SharedPreferencesUtils.loadLong(context, ProjectConstants.SAVED_SURPRISE_ENDING_MILLISECONDS, currentMilliseconds);

            long newSurpriseEndingMilliseconds = surpriseEndingMilliseconds + surprisePeriod;

            //if the device is turned off for the long time or any other reason
            //newSurpriseEndingMilliseconds may be passed during the current compliment loading
            //the complimenting loop will be restarted from now
            if(newSurpriseEndingMilliseconds < currentMilliseconds){
                newSurpriseEndingMilliseconds = currentMilliseconds + surprisePeriod;
                surpriseEndingMilliseconds = currentMilliseconds;
            }

            SharedPreferencesUtils.saveLong(context, ProjectConstants.SAVED_SURPRISE_ENDING_MILLISECONDS, newSurpriseEndingMilliseconds);

            int randNum = generateRandom(surprisePeriod);
            Log.d("AppDebug", "SchedulingUtils calculateTriggerAtMilliseconds randNum: " + randNum
                    + "\nnextLaunch date: " + new Date(System.currentTimeMillis() + randNum)
                    + "\nCur Date" + new Date(System.currentTimeMillis())
                    + "\nresult in milliseconds: " + System.currentTimeMillis() + randNum

            );

            return surpriseEndingMilliseconds + randNum;
        }

        if (isScheduled) {
            String savedStringValue = SharedPreferencesUtils.loadString(context, context.getString(R.string.sp_time_wait_value), null);

            Log.d("AppDebug", "SchedulingUtils savedStringValue: " + savedStringValue);
            //return saved time in milliseconds
            long result = savedStringValue != null ? (Integer.parseInt(savedStringValue) * 60000) : DEFAULT_VALUE_IN_MILLISECONDS ;
            return System.currentTimeMillis() + result;
        }

        return DEFAULT_VALUE_IN_MILLISECONDS;
    }


    public static String checkForErrors(Context context, String enteredInt) {
        //time constants should be in minutes
        int MINIMUM_WAITING_TIME = 1; //todo  2 * 60 * 60; two hours in seconds is a minimum time
        int MAX_WAITING_TIME = 35790;
        try {
            int inputNum = (Integer.parseInt(String.valueOf(enteredInt)));

            if (inputNum < MINIMUM_WAITING_TIME || inputNum > MAX_WAITING_TIME) {
                if (inputNum < 0) {
                    throw new NumberFormatException("Just catch me to return error message!");
                }

                return context.getString(R.string.minimum_waiting_time_text) + MINIMUM_WAITING_TIME;
            }
        } catch (NumberFormatException e) {
            return context.getString(R.string.invalid_number_text);
        }

        return null;
    }

    public static int generateRandom(int num) {
        if(num != 0){
            Random random = new Random();
            return random.nextInt(num);
        }

        return num;
    }

    public static void stopComplimenting(Context context) {
        Log.d("AppDebug", "SchedulingUtils stopComplimenting called");
        AlarmsProvider.cancelAlarm(context);
    }

    private static void saveNextTriggerDate(Context context, long nextFireInMilliseconds) {
        Log.d("AppDebug", "SchedulingUtils saveNextTriggerDate isScheduled Date :  " + new Date(nextFireInMilliseconds));
        SharedPreferencesUtils.saveLong(context, ProjectConstants.SAVED_NEXT_LAUNCH_MILLISECONDS, nextFireInMilliseconds);
    }
}
