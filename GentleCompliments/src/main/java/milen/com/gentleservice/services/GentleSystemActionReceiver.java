package milen.com.gentleservice.services;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import java.util.Calendar;
import java.util.Date;

import milen.com.gentleservice.R;
import milen.com.gentleservice.ui.activities.ComplimentActivity;
import milen.com.gentleservice.utils.AppNotificationManager;
import milen.com.gentleservice.utils.CalendarUtils;
import milen.com.gentleservice.utils.SchedulingUtils;
import milen.com.gentleservice.utils.SharedPreferencesUtils;


public class GentleSystemActionReceiver extends BroadcastReceiver {
    public static final String ACTION_START_COMPLIMENT = "milen.com.gentleservice.action.START_COMPLIMENT";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("AppDebug", "GentleSystemActionReceiver got action:" + action);

        if (action != null && action.equalsIgnoreCase(ACTION_START_COMPLIMENT)) {

            scheduleNextTask(context);

            if (shouldAddNotification(context)) {
                // Create an Intent for the activity you want to start
                Intent resultIntent = new Intent(context, ComplimentActivity.class);
                // Create the TaskStackBuilder and add the intent, which inflates the back stack
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntentWithParentStack(resultIntent);

                // Get the PendingIntent containing the entire back stack
                PendingIntent resultPendingIntent =  stackBuilder.getPendingIntent(
                        AppNotificationManager.getUnusedInt(),
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

                AppNotificationManager.addNotificationOnPane(context, resultPendingIntent);

                return;
            }


            fireCompliment(context);
        }
        /*if(action != null) {
            switch (action) {
                case Intent.ACTION_LOCALE_CHANGED:
                    //Log.d("AppDebug","GentleSystemActionReceiver ACTION_LOCALE_CHANGED");
                    manageLocaleChanges(context);
                    break;

              *//*  case Intent.ACTION_BOOT_COMPLETED:
                    //Log.d("AppDebug","GentleSystemActionReceiver ACTION_BOOT_COMPLETED");
                    handleBootAction(context);
                    break;*//*

                case ACTION_MANAGE_AFTER_LOCALE_CHANGED:
                    //Log.d("AppDebug", "GentleSystemActionReceiver get ACTION_MANAGE_AFTER_LOCALE_CHANGED");
                    //manageAction(context);
                    break;

                default:
                    break;
            }
        }*/
    }

    private void scheduleNextTask(Context context) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        dispatcher.cancel(AlarmsProvider.TAG);
        dispatcher.schedule(makeJob(context, dispatcher));
    }

    private static Job makeJob(Context context,FirebaseJobDispatcher dispatcher) {
        int type = SharedPreferencesUtils.loadInt(context, SchedulingUtils.TYPE_KEY, -1);
        int period = SharedPreferencesUtils.loadInt(context, SchedulingUtils.PERIOD_KEY, -1);
        int fireAfter = period;
        int currentRandom = SharedPreferencesUtils.loadInt(context, SchedulingUtils.RANDOM_VALUE_KEY, -1);

        int nextRandom = -1;
        if (type == SchedulingUtils.SURPRISE) {
            nextRandom = SchedulingUtils.generateRandomMinutes(period);
            fireAfter = SchedulingUtils.calculateNextFireAfter(period, currentRandom, nextRandom);
            SharedPreferencesUtils.saveInt(context, SchedulingUtils.RANDOM_VALUE_KEY, nextRandom);
            SharedPreferencesUtils.saveInt(context, SchedulingUtils.FIRE_AFTER_KEY, fireAfter);
        }

        Long shuldFire= System.currentTimeMillis()+fireAfter;
        Log.d("AppDebug", "makeJob extras at:" + new Date(System.currentTimeMillis()) +
                "\ntype: " + type +
                " period: " + period +
                " fire after: " + fireAfter +
                " cur random: " + currentRandom +
                " next random: " + nextRandom +
                " SHOULD_FIRE " + new Date(shuldFire)
        );

        SharedPreferencesUtils.saveLong(context, SchedulingUtils.SHOULD_FIRE_KEY,
                (System.currentTimeMillis()+fireAfter));

        return dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(AlarmsProvider.class)
                // uniquely identifies the job
                .setTag(AlarmsProvider.TAG)
                // one-off job
                .setRecurring(true)
                // don't persist past a device reboot
                .setLifetime(Lifetime.FOREVER)
                // start between fireAfter - 1 and fireAfter seconds from now
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
                .build();
    }

    /*private void handleBootAction(Context context) {
        boolean isServiceRunning = SharedPreferencesUtils
                .loadBoolean(context, context.getString(R.string.sp_is_service_running), false);
        if(isServiceRunning){
            manageAction(context);
        }
    }*/

   /* private void manageAction(Context context) {
        long fireAtMilliseconds = SharedPreferencesUtils.loadLong(context, ProjectConstants.SAVED_NEXT_LAUNCH_MILLISECONDS, System.currentTimeMillis());

        //to insure we get the fireAtMilliseconds one second less
        if(fireAtMilliseconds - 1000 > System.currentTimeMillis()){
            //Log.d("AppDebug","GentleSystemActionReceiver must wait more");
            SchedulingUtils.startComplimentingJob(context);
            //alarmsProvider.(context, fireAtMilliseconds);
        } else {
            //Log.d("AppDebug","GentleSystemActionReceiver  must should fire activity");
            Intent startingCompliment = new Intent(context, ComplimentActivity.class);
            startingCompliment.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startingCompliment);
        }
    }*/



  /*  private void manageLocaleChanges(Context context){
        DbLocaleTextChanger dbManager = new DbLocaleTextChanger();
        dbManager.execute(context);
    }*/

    private static void fireCompliment(Context context) {
        Intent complimentIntent = new Intent(context, ComplimentActivity.class);
        complimentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
    }
}
