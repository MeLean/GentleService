package com.meline.gentleservice.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.meline.gentleservice.ProjectConstants;
import com.meline.gentleservice.R;
import com.meline.gentleservice.ui.activities.ComplimentActivity;
import com.meline.gentleservice.utils.SharedPreferencesUtils;


public class GentleSystemActionReceiver extends BroadcastReceiver {
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
                    manageRebooting(context);
                }
                break;

            default:
                break;
        }
}

    private void manageRebooting(Context context) {
        long fireAtMilliseconds = SharedPreferencesUtils.loadLong(context, ProjectConstants.SAVED_NEXT_LAUNCH_MILLISECONDS, System.currentTimeMillis());

        //to insure we get the fireAtMilliseconds one second less
        if(fireAtMilliseconds - 1000 > System.currentTimeMillis()){
            Log.d("AppDebug","ACTION_BOOT_COMPLETED must wait more");
            AlarmsProvider alarmsProvider = new AlarmsProvider();
            alarmsProvider.setAlarm(context, fireAtMilliseconds);
        } else {
            Log.d("AppDebug","ACTION_BOOT_COMPLETED must should fire activity");
            Intent startingCompliment = new Intent(context, ComplimentActivity.class);
            startingCompliment.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startingCompliment);
        }
    }

    private void manageComplimentsLocaleChanges(Context context){
        //todo manage Locale changes
    }
}
