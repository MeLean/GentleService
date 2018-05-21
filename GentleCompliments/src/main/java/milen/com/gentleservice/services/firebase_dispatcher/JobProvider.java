package milen.com.gentleservice.services.firebase_dispatcher;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;


import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.lang.ref.WeakReference;
import java.util.Date;


public class JobProvider extends JobService {
    static final String TAG = "milen.com.gentleservice.job_alarm_tag";
    LaunchBroadcast launchBroadcast;

    @Override
    public boolean onStartJob(JobParameters job) {
        Log.d("AppDebug", "onStartJob at " + new Date());

        launchBroadcast = new LaunchBroadcast(getApplicationContext(), (b) -> jobFinished(job, !b));
        launchBroadcast.execute();
        return true; // Answers the question: "Is there still work going on?"
    }


    @Override
    public boolean onStopJob(JobParameters job) {
        Log.d("AppDebug", "onStopJob at " + new Date());
        return true; // Answers the question: "Should this job be retried?"
    }

    private static class LaunchBroadcast extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<Context> applicationContextReference;
        private ResultListener listener;

        // only retain a weak reference to the activity
        LaunchBroadcast(Context context, ResultListener resultListener) {
            applicationContextReference = new WeakReference<>(context);
            listener = resultListener;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            broadcastComplimentIntent(applicationContextReference.get());
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            listener.onResult(result);
        }

        interface ResultListener {
            void onResult(boolean result);
        }
    }


    private static void broadcastComplimentIntent(Context context) {
        Intent broadCast = new Intent();
        broadCast.setAction(GentleSystemActionReceiver.ACTION_START_COMPLIMENT);
        context.sendBroadcast(broadCast);
    }
}

