package milen.com.gentleservice.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import milen.com.gentleservice.R;

public class AppNotificationManager {
    private static final String GENTLE_COMPLIMENTS_GROUP = "milen.com.gentleservice.GROUP";

    public static void addNotificationOnPane(Context context, Intent intentLoad, String message) {
        //Log.d("AppDebug", "Notification must be added");
        int notificationId = getUnusedInt();

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        notificationId,
                        intentLoad,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        addNotificationOnPane(context, message, resultPendingIntent);
    }

    public static int getUnusedInt() {
        return (int)(System.currentTimeMillis() / 1000);
    }

    private static void addNotificationOnPane(Context context, String message, PendingIntent resultPendingIntent) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, context.getString(R.string.default_notification_channel_id))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setWhen(System.currentTimeMillis())
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setTimeoutAfter(Long.MAX_VALUE)
                        //set no defaults values
                        .setDefaults(0)
                        .setContentIntent(resultPendingIntent)
                        //set gourp
                        .setGroup(GENTLE_COMPLIMENTS_GROUP)
                        // Sets remove after click
                        .setAutoCancel(true)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message));
        

        // Gets an instance of the AppNotificationManager service
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(context);
        // Builds the notification and issues it.
        mNotifyMgr.notify(getUnusedInt(), mBuilder.build());
    }

    public static void addNotificationOnPane(Context context, PendingIntent intent) {
        addNotificationOnPane(context, context.getString(R.string.notify_unread_compliment_text), intent);
    }
}
