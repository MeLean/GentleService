package milen.com.gentleservice.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import milen.com.gentleservice.ui.activities.StartingActivity;

public class PhoenixService extends Service {
    public static final String REBIRTH_KEY = "here_we_go_again";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("AppDebug", "PhoenixService Service Started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rebirthApp();

        Log.d("AppDebug", "PhoenixService Service Destroyed");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("AppDebug", "PhoenixService KILLED");
        rebirthApp();
        stopSelf();
    }

    private void rebirthApp() {
        Intent intent = new Intent(getApplicationContext(), StartingActivity.class);
        intent.putExtra(REBIRTH_KEY, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
