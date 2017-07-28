package com.meline.gentleservice.ui.activities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.meline.gentleservice.ProjectConstants;
import com.meline.gentleservice.api.database.DBHelper;
import com.meline.gentleservice.utils.CalendarUtils;
import com.meline.gentleservice.api.objects_model.Compliment;
import com.meline.gentleservice.R;
import com.meline.gentleservice.utils.SchedulingUtils;
import com.meline.gentleservice.utils.SharedPreferencesUtils;

public class ComplimentActivity extends AppCompatActivity implements View.OnClickListener {
    private Compliment mCompliment;
    private Vibrator mVibrator = null;
    private TextView mComplimentContainer;
    private InterstitialAd mInterstitialAd;

    private static final String SHOW_COMPLIMENT_ONLY = "was_started_from_notification";
    private static final String SAVED_COMPLIMENT_TEXT = "saved_compliment_text";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compliment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(getAddListener());

        boolean mustOnlyLaunchCompliment = getIntent().getBooleanExtra(SHOW_COMPLIMENT_ONLY, false);
               //when you are in not disturbMode but the activity is launched by notification
        //it will vibrate, it also will vibrate if doNotDisturb mode is off
        //it wont vibrate if doNotDisturb mode is on and activity is started from ComplimentService
        if (mustOnlyLaunchCompliment) {
            launchCompliment(savedInstanceState);
        } else {
            //first check if activity has to show or has to add a Notification
            SchedulingUtils.startComplimentingJob(this);
            boolean isDoNotDisturbMode = SharedPreferencesUtils.loadBoolean(this, getString(R.string.sp_do_not_disturb), true);
            if(isDoNotDisturbMode){
                checkForDisturbPeriod();
            }

            launchCompliment(savedInstanceState);
        }
    }

    private void checkForDisturbPeriod() {
        String firstTime = SharedPreferencesUtils.loadString(this, getString(R.string.sp_start_time), getString(R.string.default_start_time));
        String secondTime = SharedPreferencesUtils.loadString(this, getString(R.string.sp_end_time), getString(R.string.default_end_time));
        String TIME_SEPARATOR = ":";
        String[] firstTimeArr = firstTime.split(TIME_SEPARATOR);
        String[] secondTimeArr = secondTime.split(TIME_SEPARATOR);

        Calendar calendar = Calendar.getInstance();
        int currentHours = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinutes = calendar.get(Calendar.MINUTE);

        long startTimeInMilliseconds =
                CalendarUtils.getMillisecondsFromTime(Integer.parseInt(firstTimeArr[0]), Integer.parseInt(firstTimeArr[1]));
        long currentHoursInMilliseconds = CalendarUtils.getMillisecondsFromTime(currentHours, currentMinutes);
        long endTimeInMilliseconds =
                CalendarUtils.getMillisecondsFromTime(Integer.parseInt(secondTimeArr[0]), Integer.parseInt(secondTimeArr[1]));
        boolean isInDisturbPeriod =
                CalendarUtils.checkIsBetween(startTimeInMilliseconds, currentHoursInMilliseconds, endTimeInMilliseconds);

        if (isInDisturbPeriod) {
            addNotificationOnPane((int) System.currentTimeMillis() / 1000);//guarantee unique ID for a second :D and every time client receive new notification
            finish();
            System.exit(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings_finish, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case R.id.action_return:
                launchAd();
                finish();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(ComplimentActivity.this, StartActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        // if no compliments quits
        if (mComplimentContainer.getText().equals(getString(R.string.no_loadable_compliments))) {
            this.cancelVibrator();
            this.finish();
            return;
        }

        switch (view.getId()) {
            case R.id.imgLike:
                launchAd();
                this.cancelVibrator();
                this.finish();
                break;

            case R.id.imgSMS:
                launchAd();
                this.cancelVibrator();
                Intent intentSms = new Intent(this, ShareComplimentActivity.class);
                intentSms.putExtra(getString(R.string.sp_sms_text), mComplimentContainer.getText().toString());
                this.startActivity(intentSms);
                break;

            case R.id.imgDislike:
                launchAd();
                DBHelper db = DBHelper.getInstance(this);


                try {
                    db.changeIsHatedStatus(mCompliment.getContent(), true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                Toast toast = Toast.makeText(this, String.format(getString(R.string.was_written_in_hated_list),
                        mCompliment.getContent()), Toast.LENGTH_SHORT);
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                if (v != null) {
                    v.setGravity(Gravity.CENTER);
                }
                toast.show();

                this.cancelVibrator();
                this.finish();
                break;
            default:
                Toast.makeText(ComplimentActivity.this, R.string.i_do_not_know_what_to_do, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_COMPLIMENT_TEXT, mComplimentContainer.getText().toString());
    }

    @Override
    public void onBackPressed() {
        launchAd();
        super.onBackPressed();
    }

    private void launchCompliment(Bundle savedInstanceState) {
        makeHeartBeatVibrate();
        ImageView imgLike = (ImageView) findViewById(R.id.imgLike);
        imgLike.setOnClickListener(this);
        ImageView imgSMS = (ImageView) findViewById(R.id.imgSMS);
        imgSMS.setOnClickListener(this);
        ImageView imgDislike = (ImageView) findViewById(R.id.imgDislike);
        imgDislike.setOnClickListener(this);

        mComplimentContainer = (TextView) findViewById(R.id.tw_compliment_conteiner);

        RelativeLayout rlContainer = (RelativeLayout) findViewById(R.id.rlContainer);
        int[] backgroundIds = getBackgrounds();
        rlContainer.setBackgroundResource(backgroundIds[new Random().nextInt(backgroundIds.length)]);

        if (savedInstanceState == null) {
            mComplimentContainer.setText(getComplimentFromDatabase());
        } else {
            mComplimentContainer.setText(savedInstanceState.getString(SAVED_COMPLIMENT_TEXT));
        }
    }

    private void makeHeartBeatVibrate() {
        Object getVibrator = this.getSystemService(Context.VIBRATOR_SERVICE);
        boolean isVibratorOn = SharedPreferencesUtils.loadBoolean(this,ProjectConstants.SAVED_VIBRATION_STATUS, true);
        if (getVibrator != null && isVibratorOn) {
            mVibrator = (Vibrator) getVibrator;
            long[] heartBeatPattern = {0,100,250,125,700,100,250,125};//old {0,100,50,125,600,100,50,125,600,100,50,125}; //heartbeat interval constants
            mVibrator.vibrate(heartBeatPattern, -1);
        }
    }

    private void cancelVibrator() {
        if (mVibrator != null) {
            mVibrator.cancel();
        }
    }

    private void addNotificationOnPane(int notificationId) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.gentle_service)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notify_unread_compliment_text));

        Intent intentLoad = new Intent(this, this.getClass());
        intentLoad.putExtra(SHOW_COMPLIMENT_ONLY, true);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        notificationId,
                        intentLoad,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        // Sets remove after click
        mBuilder.setAutoCancel(true);
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(notificationId, mBuilder.build());

    }

    public AdListener getAddListener() {
        return new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }
        };
    }

    public String getComplimentFromDatabase() {
        DBHelper db = DBHelper.getInstance(this);
        ArrayList<Compliment> compliments = null;

        try {
            compliments = db.getLoadableComplements();
        } catch (ParseException | SQLException e) {
            e.printStackTrace();
        }

        if (compliments != null) {
            int complimentsSize = compliments.size();
            if (complimentsSize > 0) {
                int random = new Random().nextInt(compliments.size());
                mCompliment = compliments.get(random);
                String result = mCompliment.getContent();

                try {
                    db.makeComplimentLoaded(mCompliment.getId());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (complimentsSize - 1 <= 0) { //last loadable compliment was loaded

                    try {
                        db.resetComplimentsToNotLoaded();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                return result;
            }
        }

        return getString(R.string.no_loadable_compliments);
    }

    private int[] getBackgrounds() {
        return new int[]{
                R.drawable.bg_one,
                R.drawable.bg_two,
                R.drawable.bg_four,
                R.drawable.bg_five,
                R.drawable.bg_three,
                R.drawable.bg_six
        };
    }

    private void launchAd() {
        int num = SchedulingUtils.generateRandom(100);
        if (num <= 100) {//todo make 50% chance to fire a interstitial mInterstitialAd
            if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
        }
    }
}
