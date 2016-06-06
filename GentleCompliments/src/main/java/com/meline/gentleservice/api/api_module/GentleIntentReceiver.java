package com.meline.gentleservice.api.api_module;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.meline.gentleservice.ui.activities.GentleCompliments;
import com.meline.gentleservice.R;
import com.meline.gentleservice.utils.SharedPreferencesUtils;

public class GentleIntentReceiver extends BroadcastReceiver {
    static final String SINGLE_SHOT_ALARM = "com.meline.gentleservice.SINGLE_SHOT_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferencesUtils spUtils = new SharedPreferencesUtils(context, context.getString(R.string.sp_name));
        GentleIntentLauncher gentleIntentLauncher = GentleIntentLauncher.getInstance();
        String action = intent.getAction();
        boolean isServiceRunning;
        try {
            isServiceRunning = spUtils.getBooleanFromSharedPreferences(context.getString(R.string.sp_isServiceRunning));
        } catch (RuntimeException e) {
            isServiceRunning = false;
        }

        switch (action) {

            case SINGLE_SHOT_ALARM:
                startMainActivityForSurpriseLoading(context);
                break;

            case "android.intent.action.BOOT_COMPLETED":
                if (isServiceRunning) {
                    long waitingTimeLeft = spUtils.getLongFromSharedPreferences(context.getString(R.string.sp_fireNextInMilliseconds));
                    String msg = String.format(context.getString(R.string.service_restarted), context.getString(R.string.app_name));
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    gentleIntentLauncher.startSingleWait(context, waitingTimeLeft);
                }
                break;

            default:
                Toast.makeText(context, context.getString(R.string.i_do_not_know_what_to_do), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void startMainActivityForSurpriseLoading(Context context) {
        Intent startActivityIntent = new Intent(context, GentleCompliments.class);
        startActivityIntent.putExtra("reloadComplimentingOnly", true);
        startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startActivityIntent);
    }
}
