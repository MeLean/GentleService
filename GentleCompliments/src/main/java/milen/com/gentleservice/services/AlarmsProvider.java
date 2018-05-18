package milen.com.gentleservice.services;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import milen.com.gentleservice.R;
import milen.com.gentleservice.ui.activities.ComplimentActivity;
import milen.com.gentleservice.utils.AppNotificationManager;
import milen.com.gentleservice.utils.CalendarUtils;
import milen.com.gentleservice.utils.SchedulingUtils;
import milen.com.gentleservice.utils.SharedPreferencesUtils;


public class AlarmsProvider extends JobService {
    static final String TAG = "milen.com.gentleservice.job_alarm_tag";

    @Override
    public boolean onStartJob(JobParameters job) {
        fireComplimentActivity(getApplicationContext());
        scheduleJob(getApplicationContext(), job.getExtras());
        jobFinished(job, isNeedToReschedule(job));
        return false; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        //Log.d("AppDebug", "onStopJob");
        //todoRemoveMeJustLogging("onStopJob", getApplicationContext(), job.getExtras());
        return true; // Answers the question: "Should this job be retried?"
    }

    private boolean isNeedToReschedule(JobParameters job) {
        return job.getExtras() != null &&
                job.getExtras().getInt(SchedulingUtils.TYPE_KEY) == SchedulingUtils.SCHEDULE;
    }


    public static void scheduleJob(Context context, Bundle extras) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        extras = SchedulingUtils.makeNewExtras(extras);

        //todo remove me
        todoRemoveMeJustLogging(context, extras);


        dispatcher.mustSchedule(makeJob(extras, dispatcher));
    }

    private static void todoRemoveMeJustLogging(Context context, Bundle extras) {
        int type = extras.getInt(SchedulingUtils.TYPE_KEY, -1);
        int period = extras.getInt(SchedulingUtils.PERIOD_KEY, -1);
        int fireAfter = extras.getInt(SchedulingUtils.FIRE_AFTER_KEY, -1);
        int currentRandom = extras.getInt(SchedulingUtils.RANDOM_VALUE_KEY, -1);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateKey = df.format(System.currentTimeMillis());
        context.getSharedPreferences("SHEDULING DEBUG", MODE_PRIVATE)
                .edit()
                .putString(dateKey, "scheduleJob extras:\n" +
                        " type: " + type +
                        " period: " + period +
                        " fire after: " + fireAfter +
                        " random: " + currentRandom
                )
                .apply();
    }

    private static Job makeJob(Bundle extras, FirebaseJobDispatcher dispatcher) {
        int type = extras.getInt(SchedulingUtils.TYPE_KEY, -1);
        int period = extras.getInt(SchedulingUtils.PERIOD_KEY, -1);
        int fireAfter = extras.getInt(SchedulingUtils.FIRE_AFTER_KEY, -1);
        int currentRandom = extras.getInt(SchedulingUtils.RANDOM_VALUE_KEY, -1);

       /* Log.d("AppDebug", "scheduleJob extras:\n" +
                " type: " + type +
                " period: " + period +
                " fire after: " + fireAfter
        );*/

        boolean isRecurring = true;
        if (type == SchedulingUtils.SURPRISE) {
            int nextRandom = SchedulingUtils.generateRandomMinutes(period);

            extras.putInt(SchedulingUtils.RANDOM_VALUE_KEY,nextRandom);
            extras.putInt(
                    SchedulingUtils.FIRE_AFTER_KEY,
                    SchedulingUtils.calculateNextFireAfter(period, currentRandom, nextRandom)
            );

            isRecurring = false;
        }

        return dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(AlarmsProvider.class)
                // uniquely identifies the job
                .setTag(TAG)
                // one-off job
                .setRecurring(isRecurring)
                // don't persist past a device reboot
                .setLifetime(Lifetime.FOREVER)
                // start between 0 and 60 seconds from now
                .setTrigger(Trigger.executionWindow(fireAfter - 1, fireAfter))
                // don't overwrite an existing job with the same tag
                .setReplaceCurrent(true)
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                /*// constraints that need to be satisfied for the job to run
                .setConstraints(
                        // only run on an unmetered network
                        Constraint.ON_UNMETERED_NETWORK,
                        // only run when the device is charging
                        Constraint.DEVICE_CHARGING
                )*/
                .setExtras(extras)
                .build();
    }

    public static void cancelJob(Context context) {
        Log.d("AppDebug", "cancelJob");
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        dispatcher.cancel(TAG);
    }

    private static void fireComplimentActivity(Context context) {
        Intent complimentIntent = new Intent(context, ComplimentActivity.class);
        complimentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (AlarmsProvider.shouldAddNotification(context)) {
            AppNotificationManager.addNotificationOnPane(context, complimentIntent);
            return;
        }


        fireCompliment(context, complimentIntent);
    }

    private static void fireCompliment(Context context, Intent complimentIntent) {
        context.startActivity(complimentIntent);
    }

    public static boolean shouldAddNotification(Context context) {
        boolean isDoNotDisturbMode = SharedPreferencesUtils.loadBoolean(context, context.getString(R.string.sp_do_not_disturb), true);
        //Log.d("AppDebug", "checkForDisturbPeriod isDoNotDisturbMode " + isDoNotDisturbMode);
        if (isDoNotDisturbMode) {
            String firstTime = SharedPreferencesUtils.loadString(context, context.getString(R.string.sp_start_time), context.getString(R.string.default_start_time));
            String secondTime = SharedPreferencesUtils.loadString(context, context.getString(R.string.sp_end_time), context.getString(R.string.default_end_time));
            String TIME_SEPARATOR = ":";
            String[] firstTimeArr = firstTime.split(TIME_SEPARATOR);
            String[] secondTimeArr = secondTime.split(TIME_SEPARATOR);

            Calendar calendar = Calendar.getInstance();
            int currentHours = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinutes = calendar.get(Calendar.MINUTE);

            long startTimeInMilliseconds =
                    CalendarUtils.getMillisecondsFromTime(Integer.parseInt(firstTimeArr[0]), Integer.parseInt(firstTimeArr[1]));
            long currentHoursInMilliseconds = CalendarUtils.getMillisecondsFromTime(currentHours, currentMinutes);
            long endTimeInMilliseconds =
                    CalendarUtils.getMillisecondsFromTime(Integer.parseInt(secondTimeArr[0]), Integer.parseInt(secondTimeArr[1]));

            return CalendarUtils.checkIsBetween(startTimeInMilliseconds, currentHoursInMilliseconds, endTimeInMilliseconds);
        } else {
            return false;
        }


       /* if (isInDisturbPeriod) {
            addNotificationOnPane((int) System.currentTimeMillis() / 1000);//guarantee unique ID for a second :D and every time client receive new notification
            finish();
            System.exit(0);
        }*/
    }
}

