package com.meline.gentleservice.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.meline.gentleservice.ProjectConstants;
import com.meline.gentleservice.R;
import com.meline.gentleservice.api.database.AsyncTaskManageDbAfterLocaleChanges;
import com.meline.gentleservice.ui.activities.ComplimentActivity;
import com.meline.gentleservice.utils.SharedPreferencesUtils;


public class GentleSystemActionReceiver extends BroadcastReceiver {
    public static final String ACTION_MANAGE_AFTER_LOCALE_CHANGED = "com.meline.gentleservice.action.MANAGE_LOCALE_CHANGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch (action) {
            case Intent.ACTION_LOCALE_CHANGED:
                //Log.d("AppDebug","GentleSystemActionReceiver ACTION_LOCALE_CHANGED");
                manageLocaleChanges(context);
                break;

            case Intent.ACTION_BOOT_COMPLETED:
                //Log.d("AppDebug","GentleSystemActionReceiver ACTION_BOOT_COMPLETED");
                handleAction(context);
                break;

            case ACTION_MANAGE_AFTER_LOCALE_CHANGED:
                //Log.d("AppDebug", "GentleSystemActionReceiver get ACTION_MANAGE_AFTER_LOCALE_CHANGED");
                handleAction(context);
                break;

            default:
                break;
        }
}

    private void handleAction(Context context) {
        boolean isServiceRunning = SharedPreferencesUtils
                .loadBoolean(context, context.getString(R.string.sp_is_service_running), false);
        if(isServiceRunning){
            manageAction(context);
        }
    }

    private void manageAction(Context context) {
        long fireAtMilliseconds = SharedPreferencesUtils.loadLong(context, ProjectConstants.SAVED_NEXT_LAUNCH_MILLISECONDS, System.currentTimeMillis());

        //to insure we get the fireAtMilliseconds one second less
        if(fireAtMilliseconds - 1000 > System.currentTimeMillis()){
            //Log.d("AppDebug","GentleSystemActionReceiver ACTION_BOOT_COMPLETED must wait more");
            AlarmsProvider alarmsProvider = new AlarmsProvider();
            alarmsProvider.setAlarm(context, fireAtMilliseconds);
        } else {
            //Log.d("AppDebug","GentleSystemActionReceiver ACTION_BOOT_COMPLETED must should fire activity");
            Intent startingCompliment = new Intent(context, ComplimentActivity.class);
            startingCompliment.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startingCompliment);
        }
    }

    private void manageLocaleChanges(Context context){
        AsyncTaskManageDbAfterLocaleChanges dbManager = new AsyncTaskManageDbAfterLocaleChanges();
        dbManager.execute(context);
    }
}
