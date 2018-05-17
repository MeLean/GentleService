package milen.com.gentleservice;

import android.app.Application;

import com.google.firebase.messaging.FirebaseMessaging;

public class GentleServices extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.default_notification_channel_id).toLowerCase());
    }
}
