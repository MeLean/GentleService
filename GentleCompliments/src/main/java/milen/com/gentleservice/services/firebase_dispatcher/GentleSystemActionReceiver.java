package milen.com.gentleservice.services.firebase_dispatcher;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import java.util.Calendar;
import java.util.Date;

import milen.com.gentleservice.R;
import milen.com.gentleservice.ui.activities.ComplimentActivity;
import milen.com.gentleservice.utils.AppNotificationManager;
import milen.com.gentleservice.utils.CalendarUtils;
import milen.com.gentleservice.utils.SharedPreferencesUtils;


public class GentleSystemActionReceiver extends BroadcastReceiver {
    public static final String ACTION_START_COMPLIMENT = "milen.com.gentleservice.action.START_COMPLIMENT";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("AppDebug", "GentleSystemActionReceiver got action:" + action);

/*        if (action != null && action.equalsIgnoreCase(ACTION_START_COMPLIMENT)) {

            scheduleNextTask(context);

            if (shouldAddNotification(context)) {
                fireNotification(context);
                return;
            }

            fireCompliment(context);
        }*/
    }
}
