package milen.com.gentleservice.services;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import milen.com.gentleservice.utils.AppNotificationManager;

public class MessagingServices extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
      // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("AppDebug", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("AppDebug", "Message data payload: " + remoteMessage.getData());
/*
            if (*//* Check if data needs to be processed by long running job *//* true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }*/
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("AppDebug", "Message Notification Body: " + remoteMessage.getNotification().getBody());
            AppNotificationManager.addNotificationOnPane(
                    getApplicationContext(),
                    new Intent(),//do nothing
                    remoteMessage.getNotification().getBody()
            );
        }
    }
}
