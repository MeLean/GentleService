package milen.com.gentleservice;

import android.app.Application;

import com.evernote.android.job.JobManager;

import milen.com.gentleservice.services.evernote_job.AppJobCreator;

public class GentleServices extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JobManager.create(this).addJobCreator(new AppJobCreator());
    }
}
