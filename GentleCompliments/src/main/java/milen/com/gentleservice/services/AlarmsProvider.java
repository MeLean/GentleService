package milen.com.gentleservice.services;


import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.util.Date;


public class AlarmsProvider extends JobService {
    static final String TAG = "milen.com.gentleservice.job_alarm_tag";

    @Override
    public boolean onStartJob(JobParameters job) {
        Log.d("AppDebug", "onStartJob at " + new Date());
        broadcastComplimentIntent(getApplicationContext());
        return false; // Answers the question: "Is there still work going on?"
    }


    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onStopJob(JobParameters job) {
        Log.d("AppDebug", "onStopJob at " + new Date());
        return true; // Answers the question: "Should this job be retried?"
    }

    public static void cancelJob(Context context) {
        Log.d("AppDebug", "cancelJob");
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        dispatcher.cancel(TAG);
    }

    private static void broadcastComplimentIntent(Context context) {
        Intent broadCast = new Intent();
        broadCast.setAction(GentleSystemActionReceiver.ACTION_START_COMPLIMENT);
        context.sendBroadcast(broadCast);
    }
}

