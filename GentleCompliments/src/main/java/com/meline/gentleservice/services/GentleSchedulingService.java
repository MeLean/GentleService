package com.meline.gentleservice.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.meline.gentleservice.ui.activities.ComplimentActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This {@code IntentService} does the app's actual work.
 * {@code SampleAlarmReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GentleSchedulingService extends IntentService {
    public GentleSchedulingService() {
        super("SchedulingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("AppDebug", "GentleSchedulingService onHandleIntent: " +  intent);
        Intent startComplimenting = new Intent(this, ComplimentActivity.class);
        startComplimenting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startComplimenting);
    }
}
