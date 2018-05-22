package milen.com.gentleservice.services.task_sheduling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


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
