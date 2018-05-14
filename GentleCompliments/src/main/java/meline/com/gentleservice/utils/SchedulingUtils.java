package meline.com.gentleservice.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobTrigger;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import meline.com.gentleservice.constants.ProjectConstants;
import meline.com.gentleservice.R;
import meline.com.gentleservice.services.AlarmsProvider;

import java.util.Date;
import java.util.Random;

public class SchedulingUtils {
    public static final int SCHEDULE = 1;
    public static final int SURPRISE = 2;

    public static final String TYPE_KEY = "type";
    public static final String PERIOD_KEY = "period";
    public static final String FIRE_AFTER_KEY = "waiting_key_fire";
    public static final String LAST_STARTED_ON_KEY = "started_on";

    private static final String TAG = "meline.com.gentleservice.SchedulingUtils_TAG";

    public static void startComplimentingJob(Context context, Bundle extras) {
        Log.d("AppDebug", "startComplimentingJob called " + new Date(System.currentTimeMillis()));
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        boolean isSurpriseMe = SharedPreferencesUtils.loadBoolean(context, context.getString(R.string.sp_surprise_me), true);
        boolean isScheduled = SharedPreferencesUtils.loadBoolean(context, context.getString(R.string.sp_is_scheduled), false);
        long calculatedTriggerAtMilliseconds = calculateTriggerAtMilliseconds(context, isSurpriseMe, isScheduled);

        saveNextTriggerDate(context, calculatedTriggerAtMilliseconds);

        /*if (scheduleType == SCHEDULE){
            extras.putLong(LAST_STARTED_ON_KEY, System.currentTimeMillis());
        }else if (scheduleType == SURPRISE) {
            extras.putLong(LAST_STARTED_ON_KEY, System.currentTimeMillis());
        }else {
            throw new UnsupportedOperationException("Unknown scheduleType: " + scheduleType);
        }*/

        dispatcher.mustSchedule(makeJob(dispatcher, extras));
    }

    private static long calculateTriggerAtMilliseconds(Context context, boolean isSurpriseMe, boolean isScheduled) {
        int DEFAULT_VALUE_IN_MILLISECONDS = 8 * 60 * 60 * 1000; //use 8 hours as default
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
            /*Log.d("AppDebug", "SchedulingUtils calculateTriggerAtMilliseconds surprisePeriod: " + surprisePeriod /60000 + " minutes"
                    + "\nrandNum: " + randNum / 60000d  + " minutes"
                    + "\nCur Date" + new Date(currentMilliseconds)
                    + "\nnextMaxTime date: " + new Date(newSurpriseEndingMilliseconds)
                    + "\nresult launch: " + new Date(surpriseEndingMilliseconds + randNum)

            );*/

            return surpriseEndingMilliseconds + randNum;
        }

        if (isScheduled) {
            String savedStringValue = SharedPreferencesUtils.loadString(context, context.getString(R.string.sp_time_wait_value), null);

            //Log.d("AppDebug", "SchedulingUtils savedStringValue: " + savedStringValue);
            //return saved time in milliseconds
            long result = savedStringValue != null ? (Integer.parseInt(savedStringValue) * 60000) : DEFAULT_VALUE_IN_MILLISECONDS ;
            return System.currentTimeMillis() + result;
        }

        return DEFAULT_VALUE_IN_MILLISECONDS;
    }

    public static int generateRandom(int num) {
        if(num != 0){
            Random random = new Random();
            return random.nextInt(num);
        }

        return num;
    }

    public static void stopComplimenting(Context context) {
        //Log.d("AppDebug", "SchedulingUtils stopComplimenting called");
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        dispatcher.cancel(TAG);
    }

    private static void saveNextTriggerDate(Context context, long nextFireInMilliseconds) {
        //Log.d("AppDebug", "SchedulingUtils saveNextTriggerDate isScheduled Date :  " + new Date(nextFireInMilliseconds));
        SharedPreferencesUtils.saveLong(context, ProjectConstants.SAVED_NEXT_LAUNCH_MILLISECONDS, nextFireInMilliseconds);
    }


    private static Job makeJob(FirebaseJobDispatcher dispatcher, Bundle extras) {
        return dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(AlarmsProvider.class)
                // uniquely identifies the job
                .setTag(TAG)
                // one-off job
                .setRecurring(false)
                // don't persist past a device reboot
                .setLifetime(Lifetime.FOREVER)
                // start between 0 and 60 seconds from now
                .setTrigger(JobDispatcherUtils.periodicTrigger(30, 1))
                // don't overwrite an existing job with the same tag
                .setReplaceCurrent(false)
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                // constraints that need to be satisfied for the job to run
               /* .setConstraints(
                        // only run on an unmetered network
                        Constraint.ON_UNMETERED_NETWORK,
                        // only run when the device is charging
                        Constraint.DEVICE_CHARGING
                )  */
                .setExtras(extras)
                .build();
    }

    public static class InputValidator {
        public static String validate(Context context, String enteredInt) {
            //time constants should be in minutes
            int MINIMUM_WAITING_TIME = 2 * 60; //two hours in minutes is a minimum time
            int MAX_WAITING_TIME = 35790;
            try {
                int inputNum = (Integer.parseInt(String.valueOf(enteredInt)));

                if (inputNum < MINIMUM_WAITING_TIME || inputNum > MAX_WAITING_TIME) {
                    if (inputNum < 0) {
                        //Just trow me to return error message!
                        throw new NumberFormatException("");
                    }

                    return context.getString(R.string.minimum_waiting_time_text) + MINIMUM_WAITING_TIME;
                }
            } catch (NumberFormatException e) {
                return context.getString(R.string.invalid_number_text);
            }

            return null;
        }
    }


    private static class JobDispatcherUtils {
        public static JobTrigger periodicTrigger(int frequency, int tolerance) {
            return Trigger.executionWindow(frequency - tolerance, frequency);
        }
    }


}
