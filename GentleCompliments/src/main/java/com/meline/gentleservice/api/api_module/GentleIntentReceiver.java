package com.meline.gentleservice.api.api_module;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.meline.gentleservice.ui.activities.MainActivity;
import com.meline.gentleservice.R;
import com.meline.gentleservice.utils.SharedPreferencesUtils;

public class GentleIntentReceiver extends BroadcastReceiver {
    static final String SINGLE_SHOT_ALARM = "com.meline.gentleservice.SINGLE_SHOT_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferencesUtils spUtils = new SharedPreferencesUtils(context, context.getString(R.string.sp_name));
        String action = intent.getAction();
        boolean isServiceRunning;
        try {
            isServiceRunning = spUtils.getBooleanFromSharedPreferences(context.getString(R.string.sp_isServiceRunning));
        } catch (RuntimeException e) {
            isServiceRunning = false;
        }

        switch (action) {
            case SINGLE_SHOT_ALARM:
                startMainActivityJustForComplimenting(context);
                break;

            case "android.intent.action.BOOT_COMPLETED":
                if (isServiceRunning) {
                    String msg = String.format("%1$s %2$s",
                            context.getString(R.string.app_name),
                            context.getString(R.string.service_restarted));

                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    startMainActivityJustForComplimenting(context);
                }
                break;

            default:
                Toast.makeText(context, context.getString(R.string.i_do_not_know_what_to_do), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void startMainActivityJustForComplimenting(Context context) {
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        startActivityIntent.putExtra("reloadComplimentingOnly", true);
        startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startActivityIntent);
    }
}
