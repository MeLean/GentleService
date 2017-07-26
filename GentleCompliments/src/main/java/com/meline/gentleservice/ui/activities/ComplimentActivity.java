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
import java.util.Random;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.meline.gentleservice.api.database.DBHelper;
import com.meline.gentleservice.utils.CalendarUtils;
import com.meline.gentleservice.api.objects_model.Compliment;
import com.meline.gentleservice.R;
import com.meline.gentleservice.utils.LocaleManagementUtils;
import com.meline.gentleservice.utils.SharedPreferencesUtils;

public class ComplimentActivity extends AppCompatActivity implements View.OnClickListener {
    private Compliment mCompliment;
    private Vibrator mVibrator = null;
    private TextView mContainer;
    InterstitialAd mInterstitialAd;
    SharedPreferencesUtils spUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        boolean checkDisturbed = getIntent().getBooleanExtra(getString(R.string.sp_disturb_check), true);
        boolean isDoNotDisturb = false;
        spUtils = new SharedPreferencesUtils(this, getString(R.string.sp_name));
        if (checkDisturbed) {
            isDoNotDisturb = spUtils.getBooleanFromSharedPreferences(getString(R.string.sp_do_not_disturb));
            if (isDoNotDisturb) {
                String firstTime = spUtils.getStringFromSharedPreferences(getString(R.string.sp_start_time));
                String secondTime = spUtils.getStringFromSharedPreferences(getString(R.string.sp_end_time));
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
                    showNotification((int) System.currentTimeMillis() / 1000);//every time will receive new notification
                    finish();
                    return;
                }
            }
        }
        setContentView(R.layout.activity_compliment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();

        makeHeartBeatVibrate();
        ImageView imgLike = (ImageView) findViewById(R.id.imgLike);
        ImageView imgSMS = (ImageView) findViewById(R.id.imgSMS);
        ImageView imgDislike = (ImageView) findViewById(R.id.imgDislike);
        imgLike.setOnClickListener(this);
        imgSMS.setOnClickListener(this);
        imgDislike.setOnClickListener(this);
        //loads random background
        RelativeLayout rlContainer = (RelativeLayout) findViewById(R.id.rlContainer);
        int[] backgroundIds = new int[]{
                R.drawable.bg_one,
                R.drawable.bg_two,
                R.drawable.bg_four,
                R.drawable.bg_five,
                R.drawable.bg_three,
                R.drawable.bg_six
        };
        rlContainer.setBackgroundResource(backgroundIds[new Random().nextInt(backgroundIds.length)]);
        DBHelper db = DBHelper.getInstance(this);
        ArrayList<Compliment> compliments = null;

        try {
            compliments = db.getLoadableComplements();
        } catch (ParseException | SQLException e) {
            e.printStackTrace();
        }


        mContainer = (TextView) findViewById(R.id.twContainer);
        String complimentStr = getString(R.string.no_loadable_compliments);
        if (compliments != null) {
            int complimentsSize = compliments.size();
            if (complimentsSize > 0) {
                int random = new Random().nextInt(compliments.size());
                mCompliment = compliments.get(random);
                complimentStr = mCompliment.getContent();
                mContainer.setText(complimentStr);

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

            } else {
                mContainer.setText(complimentStr);
            }
        }

        LocaleManagementUtils localeManagementUtils = new LocaleManagementUtils(this);
        localeManagementUtils.manageLocale(spUtils, spUtils);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });
        requestNewInterstitial();

        Random random = new Random();
        int num = random.nextInt(100);
        if (num <= 50) {// 50% chance to fire a interstitial mInterstitialAd
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
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
        if (mContainer.getText().equals(getString(R.string.no_loadable_compliments))) {
            this.cancelVibrator();
            this.finish();
            return;
        }

        switch (view.getId()) {
            case R.id.imgLike:
                this.cancelVibrator();
                this.finish();
                break;

            case R.id.imgSMS:
                this.cancelVibrator();
                Intent intentSms = new Intent(this, SmsActivity.class);
                intentSms.putExtra(getString(R.string.sp_sms_text), mCompliment.getContent());
                this.startActivity(intentSms);
                this.finish();
                break;

            case R.id.imgDislike:
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

    private void makeHeartBeatVibrate() {
        Object getVibrator = this.getSystemService(Context.VIBRATOR_SERVICE);
        if (getVibrator != null) {
            mVibrator = (Vibrator) getVibrator;
            long[] heartBeatPattern = {0, 100, 50, 125, 600, 100, 50, 125, 600, 100, 50, 125}; //heartbeat interval constants
            mVibrator.vibrate(heartBeatPattern, -1);
        }
    }

    private void cancelVibrator() {
        if (mVibrator != null) {
            mVibrator.cancel();
        }
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .setGender(AdRequest.GENDER_FEMALE)
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    private void showNotification(int notificationId) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.gentle_service)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notify_unread_compliment_text));

        Intent intentLoad = new Intent(this, this.getClass());
        intentLoad.putExtra(getString(R.string.sp_disturb_check), false);
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

}
