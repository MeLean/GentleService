package milen.com.gentleservice;

import android.app.Application;
import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Locale;

public class GentleServices extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseMessaging.getInstance().subscribeToTopic(Locale.getDefault().toString());
    }
}
