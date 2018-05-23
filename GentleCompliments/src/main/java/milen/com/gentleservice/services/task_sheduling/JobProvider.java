package milen.com.gentleservice.services.task_sheduling;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.lang.ref.WeakReference;
import java.util.Date;


public class JobProvider extends JobService {
    static final String TAG = "milen.com.gentleservice.job_alarm_tag";
    LaunchTaskAsync launchTaskAsync;

    @Override
    public boolean onStartJob(JobParameters job) {
        //Log.d("AppDebug", "onStartJob at " + new Date());
        launchTaskAsync = new LaunchTaskAsync(getApplicationContext(), (b) ->{
            jobFinished(job, !b);
            startNextJob(getApplicationContext());
        });
        launchTaskAsync.execute();
        return true; // Answers the question: "Is there still work going on?"
    }


    @Override
    public boolean onStopJob(JobParameters job) {
        //Log.d("AppDebug", "onStopJob at " + new Date());
        return false; // Answers the question: "Should this job be retried?"
    }

    private static class LaunchTaskAsync extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<Context> applicationContextReference;
        private ResultListener listener;

        // only retain a weak reference to the activity
        LaunchTaskAsync(Context context, ResultListener resultListener) {
            applicationContextReference = new WeakReference<>(context);
            listener = resultListener;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            launchCompliment(applicationContextReference.get());
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


    private static void launchCompliment(Context context) {
        SchedulingUtils schedulingUtils = new SchedulingUtils(context);
        schedulingUtils.launchCompliment();
    }

    private static void startNextJob(Context context) {
        //Log.d("AppDebug","startNextJob at" + new Date());
        SchedulingUtils schedulingUtils = new SchedulingUtils(context);
        schedulingUtils.scheduleNextTask();
    }
}

