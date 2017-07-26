package com.meline.gentleservice.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.meline.gentleservice.R;
import com.meline.gentleservice.utils.SharedPreferencesUtils;

public class GentleSystemActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch (action) {
            case Intent.ACTION_LOCALE_CHANGED:
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
                        manageisScheduledMode(context);
                    }
                }
                Log.d("AppDebug","ACTION_LOCALE_CHANGED");
                break;

            case Intent.ACTION_BOOT_COMPLETED:
                Log.d("AppDebug","ACTION_BOOT_COMPLETED");
                break;

            default:
                break;
        }
    }

    private void manageisScheduledMode(Context context) {
    }

    private void manageSurpriseMeMode(Context context) {
        //todo
    }

    private void manageComplimentsLocaleChanges(Context context){
        //todo
    }
}
