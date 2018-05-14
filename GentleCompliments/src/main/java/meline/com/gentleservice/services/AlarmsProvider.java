package meline.com.gentleservice.services;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.util.Date;

import meline.com.gentleservice.ui.activities.ComplimentActivity;
import meline.com.gentleservice.utils.SchedulingUtils;

/**
 * When the alarm fires, this WakefulBroadcastReceiver receives the broadcast Intent
 * and then starts the IntentService {@code GentleSchedulingService} to do some work.
 */
public class AlarmsProvider extends JobService {

    @Override
    public boolean onStartJob(JobParameters job) {
        Log.d("AppDebug", "onStartJob:  " + new Date(System.currentTimeMillis()));
        fireComplimentActivity();
        startNextJobFrom(job);
        return false;// Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Log.d("AppDebug", "onStopJob:  " + new Date(System.currentTimeMillis()));
        return false;// Answers the question: "Should this job be retried?"
    }

    private void fireComplimentActivity(){
        Intent dialogIntent = new Intent(this, ComplimentActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(dialogIntent);
    }

    private void startNextJobFrom(JobParameters job) {
        Bundle extras = job.getExtras();

        SchedulingUtils.startComplimentingJob(getApplicationContext(), makeNextExtras(extras));
    }

    private Bundle makeNextExtras(Bundle extras) {
        //todo
        return extras;
    }
}



 /*   @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d("AppDebug", "AlarmsProvider onReceive: " + intent);
        Intent service = new Intent(context, GentleSchedulingService.class);

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, service);
        // END_INCLUDE(alarm_onreceive)
    }

    // BEGIN_INCLUDE(set_alarm)

    public void setAlarm(Context context, long triggerAtMilliseconds) {
        //Log.d("AppDebug", "AlarmsProvider setAlarm millisecondsOfNextLaunch: " + triggerAtMilliseconds + " millisecondsOfNextLaunch date: " + new Date(triggerAtMilliseconds));
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


        PendingIntent alarmIntent = makeComplimentPendingIntent(context);

       *//*
        this will be a feature to launch in certain hour
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // Set the alarm's trigger time to 8:30 a.m.
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 30);
        *//*
  
        *//*
         * If you don't have precise time requirements, use an inexact repeating alarm
         * the minimize the drain on the device battery.
         * 
         * The call below specifies the alarm type, the trigger time, the interval at
         * which the alarm is fired, and the alarm's associated PendingIntent.
         * It uses the alarm type RTC_WAKEUP ("Real Time Clock" wake up), which wakes up 
         * the device and triggers the alarm according to the time of the device's clock. 
         * 
         * Alternatively, you can use the alarm type ELAPSED_REALTIME_WAKEUP to trigger 
         * an alarm based on how much time has elapsed since the device was booted. This 
         * is the preferred choice if your alarm is based on elapsed time--for example, if 
         * you simply want your alarm to fire every 60 minutes. You only need to use 
         * RTC_WAKEUP if you want your alarm to fire at a particular date/time. Remember 
         * that clock-based time may not translate well to other locales, and that your 
         * app's behavior could be affected by the user changing the device's time setting.
         * 
         * Here are some examples of ELAPSED_REALTIME_WAKEUP:
         * 
         * // Wake up the device to fire a one-time alarm in one minute.
         * alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
         *         SystemClock.elapsedRealtime() +
         *         60*1000, alarmIntent);
         *        
         * // Wake up the device to fire the alarm in 30 minutes, and every 30 minutes
         * // after that.
         * alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
         *         AlarmManager.INTERVAL_HALF_HOUR, 
         *         AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);
         *//*

        // Set the alarm to fire at approximately 8:30 a.m., according to the device's
        // clock, and to repeat once a day.
        int SDK_INT = Build.VERSION.SDK_INT;

        if (SDK_INT < Build.VERSION_CODES.KITKAT) {
            alarmMgr.set(AlarmManager.RTC_WAKEUP, triggerAtMilliseconds, alarmIntent);
        }
        else if (Build.VERSION_CODES.KITKAT <= SDK_INT  && SDK_INT < Build.VERSION_CODES.M) {
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, triggerAtMilliseconds, alarmIntent);
        }
        else if (SDK_INT >= Build.VERSION_CODES.M) {
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMilliseconds, alarmIntent);
        }

        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, GentleSystemActionReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
    // END_INCLUDE(set_alarm)

    // BEGIN_INCLUDE(cancel_alarm)
    public static void cancelAlarm(Context context) {

        //Log.d("AppDebug", "cancelAlarm managed");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(makeComplimentPendingIntent(context));


        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the 
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, GentleSystemActionReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private static PendingIntent makeComplimentPendingIntent(Context context) {
        Intent intent = new Intent(context, AlarmsProvider.class);
        return PendingIntent.getBroadcast(
                context,
                PENDING_INTENT_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }*/
    // END_INCLUDE(cancel_alarm)
