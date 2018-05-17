package milen.com.gentleservice.utils;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;


import milen.com.gentleservice.R;


public class AppNotificationManager {

    public static void addNotificationOnPane(Context context, Intent intentLoad, String message) {
        //Log.d("AppDebug", "Notification must be added");
        int notificationId = (int)(System.currentTimeMillis() / 1000);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, context.getString(R.string.default_notification_channel_id))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setWhen(System.currentTimeMillis())
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
                ;

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        notificationId,
                        intentLoad,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        // Sets remove after click
        mBuilder.setAutoCancel(true);
        // Gets an instance of the AppNotificationManager service
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(context);
        // Builds the notification and issues it.
        mNotifyMgr.notify(notificationId, mBuilder.build());
    }

    public static void addNotificationOnPane(Context context, Intent intent) {
        addNotificationOnPane(context, intent, context.getString(R.string.notify_unread_compliment_text));
    }
}
