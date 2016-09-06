package com.meline.gentleservice.api.api_module;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.meline.gentleservice.ui.activities.ComplimentActivity;
import com.meline.gentleservice.utils.CalendarUtils;
import com.meline.gentleservice.utils.SdCardWriter;

import java.util.Date;

public class GentleIntentLauncher {
    private static GentleIntentLauncher instance = new GentleIntentLauncher();

    public static GentleIntentLauncher getInstance() {
        return instance;
    }

    private GentleIntentLauncher() {
    }

    public void startComplimenting(Context context, Long scheduleWaitingTime) {

        long waiting;
        if(scheduleWaitingTime != null){
            //it is Schedule mode
            waiting = System.currentTimeMillis() + scheduleWaitingTime;
        }else{
            //it is Surprise mode
            waiting = CalendarUtils.manageWaitingTime(context);
        }

        //insurance for all ready passed period
        if(waiting > 0) {
            this.startSingleWait(context.getApplicationContext(), waiting);
            loadComplimentActivity(context.getApplicationContext());
        }else {
            this.stopComplimenting(context);
        }
    }

    public void startSingleWait(Context context, long waitingTime) {
        Intent singleIntent = new Intent(context.getApplicationContext(), GentleIntentReceiver.class);
        singleIntent.setAction(GentleIntentReceiver.SINGLE_SHOT_ALARM);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, singleIntent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, waitingTime, pendingIntent);
    }

    public void stopComplimenting(Context context) {
        Intent cancelIntent = new Intent(context.getApplicationContext(), GentleIntentReceiver.class);
        cancelIntent.setAction(GentleIntentReceiver.SINGLE_SHOT_ALARM);
        PendingIntent sender = PendingIntent.getBroadcast(context.getApplicationContext(), 0, cancelIntent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    public void loadComplimentActivity(Context context) {
        Intent intentLoad = new Intent(context, ComplimentActivity.class);
        intentLoad.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intentLoad);
    }
}
