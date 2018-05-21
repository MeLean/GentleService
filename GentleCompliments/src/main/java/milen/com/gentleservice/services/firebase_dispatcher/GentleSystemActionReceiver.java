package milen.com.gentleservice.services.firebase_dispatcher;

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
                fireNotification(context);
                return;
            }

            fireCompliment(context);
        }
    }

    private void scheduleNextTask(Context context) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        //cancel current task before run new
        dispatcher.cancel(JobProvider.TAG);
        Job newJob = makeJob(context, dispatcher);
        dispatcher.schedule(newJob);
    }

    private Job makeJob(Context context,FirebaseJobDispatcher dispatcher) {
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

        Long shouldFire = System.currentTimeMillis()+fireAfter;
        SharedPreferencesUtils.saveLong(context, SchedulingUtils.SHOULD_FIRE_KEY, shouldFire);

        Log.d("AppDebug", "makeJob extras at:" + new Date(System.currentTimeMillis()) +
                "\ntype: " + type +
                " period: " + period +
                " fire after: " + fireAfter +
                " cur random: " + currentRandom +
                " next random: " + nextRandom +
                " SHOULD_FIRE " + new Date(shouldFire)
        );

        return dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(JobProvider.class)
                // uniquely identifies the job
                .setTag(JobProvider.TAG)
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

    private void fireNotification(Context context) {
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
    }

    private void fireCompliment(Context context) {
        Intent complimentIntent = new Intent(context, ComplimentActivity.class);
        complimentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(complimentIntent);
    }

    public boolean shouldAddNotification(Context context) {
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
