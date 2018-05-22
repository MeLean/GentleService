package milen.com.gentleservice.services.task_sheduling;

import android.app.PendingIntent;
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

import milen.com.gentleservice.R;
import milen.com.gentleservice.ui.activities.ComplimentActivity;
import milen.com.gentleservice.utils.AppNotificationManager;
import milen.com.gentleservice.utils.CalendarUtils;
import milen.com.gentleservice.utils.SharedPreferencesUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class SchedulingUtils {
    public static final int SCHEDULE = 1;
    public static final int SURPRISE = 2;

    public static final String TYPE_KEY = "type";
    public static final String PERIOD_KEY = "period";
    public static final String FIRE_AFTER_KEY = "waiting_key_fire";
    public static final String RANDOM_VALUE_KEY = "random_key_fire";

    public static final String SHOULD_FIRE_KEY = "should_fire_key";

    private static final int ONE_DAY = 86400000; //default value one day
    private Context mContext;

    private int generateRandomMinutes(int num) {
        //min minutes are needed to ensure that
        //window for the periodic job is more then 15 minutes.
        int MIN_MINUTES = 15;
        int result;

        Random random = new Random();
        result = random.nextInt(num);

        return result > MIN_MINUTES ? result : MIN_MINUTES;
    }

    public void stopComplimenting() {
        //Log.d("AppDebug", "SchedulingUtils stopComplimenting called");
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(mContext));
        dispatcher.cancel(JobProvider.TAG);
    }

    private int calculateNextFireAfter(int period, int currentRandom, int nextRandom) {
        if (period > 0 && currentRandom > 0 && nextRandom > 0) {
            int timeUntilPeriodIsEnded = period - currentRandom;

            Log.d("AppDebug", " timeUntilPeriodIsEnded " + timeUntilPeriodIsEnded + " nextRandom " + nextRandom);
            int time = timeUntilPeriodIsEnded + nextRandom;
            return time > 0 ? time : (time * -1);
        }

        return ONE_DAY;
    }

    private void fireCompliment() {
        Intent complimentingIntent = new Intent(mContext, ComplimentActivity.class);
        complimentingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        mContext.startActivity(complimentingIntent);
    }

    public static class InputValidator {
        public static String validate(Context context, String enteredInt) {
            //time constants should be in minutes
            int MINIMUM_WAITING_TIME = 1; //todo an hour
            int MAX_WAITING_TIME = 10080; //a week
            try {
                int inputNum = (Integer.parseInt(String.valueOf(enteredInt)));

                if (inputNum < MINIMUM_WAITING_TIME || inputNum > MAX_WAITING_TIME) {
                    if (inputNum < 0) {
                        //Just trow me to return error message!
                        throw new NumberFormatException("");
                    }

                    return context.getString(R.string.minimum_waiting_time_text) + MINIMUM_WAITING_TIME ;
                }
            } catch (NumberFormatException e) {
                return context.getString(R.string.invalid_number_text);
            }

            return null;
        }
    }

    public SchedulingUtils(Context context){
        mContext = context;
    }

    public void scheduleNextTask() {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(mContext));
        //cancel current task before run new
        dispatcher.cancel(JobProvider.TAG);
        startNewJob(dispatcher, JobProvider.TAG);
    }


    private void startNewJob(FirebaseJobDispatcher dispatcher, String tag) {
        int type = SharedPreferencesUtils.loadInt(mContext, SchedulingUtils.TYPE_KEY, -1);
        int period = SharedPreferencesUtils.loadInt(mContext, SchedulingUtils.PERIOD_KEY, -1);
        int fireAfter = period;
        int currentRandom = SharedPreferencesUtils.loadInt(mContext, SchedulingUtils.RANDOM_VALUE_KEY, -1);

        int nextRandom = -1;
        if (type == SchedulingUtils.SURPRISE) {
            nextRandom = generateRandomMinutes(period);
            fireAfter = calculateNextFireAfter(period, currentRandom, nextRandom);
            SharedPreferencesUtils.saveInt(mContext, SchedulingUtils.RANDOM_VALUE_KEY, nextRandom);
            SharedPreferencesUtils.saveInt(mContext, SchedulingUtils.FIRE_AFTER_KEY, fireAfter);
        }

        Long shouldFire = (fireAfter * 1000) + System.currentTimeMillis();
        SharedPreferencesUtils.saveLong(mContext, SchedulingUtils.SHOULD_FIRE_KEY, shouldFire);

        Log.d("AppDebug", "startNewJob extras at:" + new Date(System.currentTimeMillis()) +
                "\ntype: " + type +
                " period: " + period +
                " fire after: " + fireAfter +
                " cur random: " + currentRandom +
                " next random: " + nextRandom +
                " SHOULD_FIRE " + new SimpleDateFormat("yyyy MM dd HH:mm:ss", Locale.getDefault()).format(shouldFire)
        );

         Job.Builder jobBuilder = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(JobProvider.class)
                // uniquely identifies the job
                .setTag(tag)
                // one-off job
                .setRecurring(true)
                // don't persist past a device reboot
                .setLifetime(Lifetime.FOREVER)
                // start between fireAfter - 1 and fireAfter seconds from now
                .setTrigger(Trigger.executionWindow(fireAfter - 1, fireAfter))
                // don't overwrite an existing job with the same tag
                .setReplaceCurrent(true)
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR);
                /*// constraints that need to be satisfied for the job to run
                .setConstraints(
                        // only run on an unmetered network
                       ,
                        // only run when the device is charging
                        Constraint.DEVICE_CHARGING
                )*/

        dispatcher.mustSchedule(jobBuilder.build());
    }

    private void fireNotification() {
        Intent resultIntent = new Intent(mContext, ComplimentActivity.class);
        //resultIntent.putExtra(ProjectConstants.JUST_SHOW_COMPLIMENT, true);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addNextIntentWithParentStack(resultIntent);

        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =  stackBuilder.getPendingIntent(
                AppNotificationManager.getUnusedInt(),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        AppNotificationManager.addNotificationOnPane(mContext, resultPendingIntent);
    }

    private boolean shouldAddNotification() {
        boolean isDoNotDisturbMode = SharedPreferencesUtils.loadBoolean(mContext, mContext.getString(R.string.sp_do_not_disturb), true);
        //Log.d("AppDebug", "checkForDisturbPeriod isDoNotDisturbMode " + isDoNotDisturbMode);
        if (isDoNotDisturbMode) {
            String firstTime = SharedPreferencesUtils.loadString(mContext, mContext.getString(R.string.sp_start_time), mContext.getString(R.string.default_start_time));
            String secondTime = SharedPreferencesUtils.loadString(mContext, mContext.getString(R.string.sp_end_time), mContext.getString(R.string.default_end_time));
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


    public void launchCompliment() {
        if (shouldAddNotification()){
            fireNotification();
        } else {
            fireCompliment();
        }
    }
}
