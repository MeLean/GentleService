package com.meline.gentleservice.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.meline.gentleservice.ProjectConstants;
import com.meline.gentleservice.R;
import com.meline.gentleservice.utils.SchedulingUtils;
import com.meline.gentleservice.utils.SharedPreferencesUtils;

import java.util.Date;

public class GentleSystemActionReceiver extends BroadcastReceiver {
    private static final int MILLISECONDS_TO_MINUTES_CONSTANT = 60000;
    private static final int MILLISECONDS_TO_SECONDS_CONSTANT = 1000;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch (action) {
            case Intent.ACTION_LOCALE_CHANGED:
                manageComplimentsLocaleChanges(context);

                Log.d("AppDebug","ACTION_LOCALE_CHANGED");
                break;

            case Intent.ACTION_BOOT_COMPLETED:
                boolean isServiceRunning = SharedPreferencesUtils
                        .loadBoolean(context, context.getString(R.string.sp_is_service_running),false);
                if(isServiceRunning){
                    boolean isSurprise = SharedPreferencesUtils
                            .loadBoolean(context, context.getString(R.string.sp_surprise_me),true);
                    if(isSurprise){
                        manageSurpriseMeMode(context);
                    }

                    boolean isSchedule = SharedPreferencesUtils
                            .loadBoolean(context, context.getString(R.string.sp_is_scheduled),true);
                    if(isSchedule){
                        manageScheduledMode(context);
                    }
                }
                break;

            default:
                break;
        }
    }

    private void manageScheduledMode(Context context) {
        long lastLaunchMilliseconds = SharedPreferencesUtils.loadLong(context, ProjectConstants.SAVED_LAST_LAUNCH_MILLISECONDS, 0);
        //120 should be  never used but just in case we put some parsable int value
        //we multiply by MILLISECONDS_TO_MINUTES_CONSTANT to convert saved minutes to milliseconds
        long launchingPeriodInMilliseconds =
                (Integer.parseInt(SharedPreferencesUtils.loadString(context, context.getString(R.string.sp_time_wait_value), "120")))
                * MILLISECONDS_TO_MINUTES_CONSTANT;
        long timeLeftToLaunch = (lastLaunchMilliseconds + launchingPeriodInMilliseconds) - System.currentTimeMillis();
        int timeLeftToLaunchInMinutes = (int) (timeLeftToLaunch / MILLISECONDS_TO_MINUTES_CONSTANT);

        Log.d("AppDebug", "manageScheduledMode after BOOT COMPLETED\nlast launched date:" + new Date(lastLaunchMilliseconds)+
                "\nlastLaunchMilliseconds:"+ lastLaunchMilliseconds + "\nlaunchingPeriodInMilliseconds: " + launchingPeriodInMilliseconds+
                "\ntimeLeftToLaunchInMinutes: " + timeLeftToLaunchInMinutes
        );

        startComplimentingJobIfTimeHasPassed(context, timeLeftToLaunchInMinutes);
    }

    private void manageSurpriseMeMode(Context context) {
        int timeToLaunch = SchedulingUtils.calculateWaitingTimeInSeconds(context, true, false);
        Log.d("AppDebug", "manageScheduledMode after BOOT COMPLETED manageSurpriseMeMode timeToLaunch: " + timeToLaunch);
        startComplimentingJobIfTimeHasPassed(context, timeToLaunch);
    }

    private void startComplimentingJobIfTimeHasPassed(Context context, int timeToLaunch) {
        if(timeToLaunch > 0){
            SchedulingUtils.startComplimentingJob(context,timeToLaunch, timeToLaunch);
        }else{
            SchedulingUtils.startComplimentingJob(context,0,0);
        }
    }

    private void manageComplimentsLocaleChanges(Context context){
        //todo
    }
}
