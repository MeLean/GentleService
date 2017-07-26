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
        String action = intent.getAction();

        switch (action) {
            case Intent.ACTION_LOCALE_CHANGED:
                Toast.makeText(context, "THE LOCALE HAS BEEN CHANGED", Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }
    }

    private void manageComplimentsLocaleChanges(Context context){
        //todo
    }
}
