package milen.com.gentleservice.ui.activities;


import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
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
import java.util.Random;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.analytics.FirebaseAnalytics;

import milen.com.gentleservice.constants.ProjectConstants;
import milen.com.gentleservice.api.database.DBHelper;
import milen.com.gentleservice.services.AlarmsProvider;
import milen.com.gentleservice.utils.AdManager;
import milen.com.gentleservice.api.objects_model.Compliment;
import milen.com.gentleservice.R;
import milen.com.gentleservice.utils.AppNotificationManager;
import milen.com.gentleservice.utils.SchedulingUtils;
import milen.com.gentleservice.utils.SharedPreferencesUtils;

public class ComplimentActivity extends AppCompatActivity implements View.OnClickListener {
    private Compliment mCompliment;
    private Vibrator mVibrator = null;
    private TextView mComplimentContainer;
    private RewardedVideoAd mRewardedAd;

    private static final String SAVED_COMPLIMENT_TEXT = "saved_compliment_text";
    private static final String SAVED_BACKGROUND_ID = "saved_background_id";
    private int mBackgroundId = 0;
    private boolean isShareClicked = false;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent startingIntent = getIntent();
        if(shouldSkipNotificationCheck(startingIntent)) {
            if (AlarmsProvider.shouldAddNotification(this)) {
                Intent intent = new Intent(this, ComplimentActivity.class);
                intent.putExtra(ProjectConstants.SKIP_NOTIFICATION_KEY, true);
                AppNotificationManager.addNotificationOnPane(this, intent);
                finish();
            }
            ;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        } else {
            //noinspection deprecation
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compliment);


        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if (AdManager.shouldLaunchAd(this)) {
            MobileAds.initialize(this, "pub-" + getString(R.string.ads_user_id));
            mRewardedAd = MobileAds.getRewardedVideoAdInstance(this);
            mRewardedAd.setRewardedVideoAdListener(getAddListener());

            loadNewAd();
        }

        launchCompliment(savedInstanceState);
    }

    private boolean shouldSkipNotificationCheck(Intent startingIntent) {
        return startingIntent.hasExtra(ProjectConstants.SKIP_NOTIFICATION_KEY);
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
                fireAnalyticContentEvent(mComplimentContainer.getText().toString(), "closed");
                onBackPressed();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(ComplimentActivity.this, StartingActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        // if no compliments quits
        if (mComplimentContainer.getText().toString().equals(getString(R.string.no_loadable_compliments))) {
            this.cancelVibrator();
            this.finish();
            return;
        }

        isShareClicked = false;

        switch (view.getId()) {
            case R.id.imgLike:
                launchAdOrDoAction();
                this.cancelVibrator();
                fireAnalyticContentEvent(mComplimentContainer.getText().toString(), "liked");
                break;

            case R.id.share:
                isShareClicked = true;
                launchAdOrDoAction();
                this.cancelVibrator();
                fireAnalyticContentEvent(mComplimentContainer.getText().toString(), "shared");
                break;

            case R.id.imgDislike:
                launchAdOrDoAction();
                fireAnalyticContentEvent(mComplimentContainer.getText().toString(), "disliked");
                DBHelper db = DBHelper.getInstance(this);


                try {
                    db.changeIsHatedStatus(mCompliment.getContent(), true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                Toast toast = Toast.makeText(this, String.format(getString(R.string.was_written_in_hated_list),
                        mCompliment.getContent()), Toast.LENGTH_SHORT);
                TextView v = toast.getView().findViewById(android.R.id.message);
                if (v != null) {
                    v.setGravity(Gravity.CENTER);
                }
                toast.show();
                this.cancelVibrator();
                break;
            default:
                Toast.makeText(ComplimentActivity.this, R.string.i_do_not_know_what_to_do, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void fireAnalyticContentEvent(String name, String type) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, SharedPreferencesUtils.loadString(this, ProjectConstants.SAVED_TOKEN, "NO_SAVED_TOKEN"));
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.LOCATION, getLocale());
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private String getLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getResources().getConfiguration().getLocales().get(0).toString();
        } else {
            //noinspection deprecation
            return getResources().getConfiguration().locale.toString();
        }
    }

    private void share() {
        isShareClicked = false;
        Intent intentSms = new Intent(this, ShareComplimentActivity.class);
        intentSms.putExtra(getString(R.string.sp_sms_text), mComplimentContainer.getText().toString());
        this.startActivity(intentSms);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_COMPLIMENT_TEXT, mComplimentContainer.getText().toString());
        outState.putInt(SAVED_BACKGROUND_ID, mBackgroundId);
    }

    @Override
    public void onBackPressed() {
        if (AdManager.shouldLaunchAd(this)) {
            launchAdOrDoAction();
        } else {
            super.onBackPressed();
        }

    }

    private void launchCompliment(Bundle savedInstanceState) {
        makeHeartBeatVibrate();
        ImageView imgLike = findViewById(R.id.imgLike);
        imgLike.setOnClickListener(this);
        ImageView share = findViewById(R.id.share);
        share.setOnClickListener(this);
        ImageView imgDislike = findViewById(R.id.imgDislike);
        imgDislike.setOnClickListener(this);

        mComplimentContainer = findViewById(R.id.tw_compliment_conteiner);

        RelativeLayout rlContainer = findViewById(R.id.rlContainer);
        int[] backgroundIds = getBackgrounds();


        if (savedInstanceState == null) {
            mComplimentContainer.setText(getComplimentFromDatabase());
            mBackgroundId = SchedulingUtils.generateRandom(backgroundIds.length);
        } else {
            mComplimentContainer.setText(savedInstanceState.getString(SAVED_COMPLIMENT_TEXT));
            mBackgroundId = savedInstanceState.getInt(SAVED_BACKGROUND_ID);
        }

        rlContainer.setBackgroundResource(backgroundIds[mBackgroundId]);
    }

    private void makeHeartBeatVibrate() {
        Object getVibrator = this.getSystemService(Context.VIBRATOR_SERVICE);
        boolean isVibratorOn = SharedPreferencesUtils.loadBoolean(this, ProjectConstants.SAVED_VIBRATION_STATUS, true);
        if (getVibrator != null && isVibratorOn) {
            mVibrator = (Vibrator) getVibrator;
            long[] heartBeatPattern = {0, 100, 250, 125, 700, 100, 250, 125};//old {0,100,50,125,600,100,50,125,600,100,50,125}; //heartbeat interval constants
            mVibrator.vibrate(heartBeatPattern, -1);
        }
    }

    private void cancelVibrator() {
        if (mVibrator != null) {
            mVibrator.cancel();
        }
    }

    public RewardedVideoAdListener getAddListener() {
        return new RewardedVideoAdListener() {

            @Override
            public void onRewardedVideoAdLoaded() {
                //Log.d("AppDebug", "ad has been loaded");
                //do nothing
            }

            @Override
            public void onRewardedVideoAdOpened() {
                //Log.d("AppDebug", "ad has been opened");
                //do nothing
            }

            @Override
            public void onRewardedVideoStarted() {
                //do nothing
            }

            @Override
            public void onRewardedVideoAdClosed() {
                //Log.d("AppDebug", "ad has been onRewardedVideoAdClosed");
                loadNewAd();
                adFinishedDoAction();
                //do nothing
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                AdManager.reward(ComplimentActivity.this);
                Toast.makeText(ComplimentActivity.this, R.string.reward_message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                //Log.d("AppDebug", "ad onRewardedVideoAdLeftApplication");
                //do nothing
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                adFinishedDoAction();
                //do nothing
            }

            @Override
            public void onRewardedVideoCompleted() {
                //Log.d("AppDebug", "ad onRewardedVideoCompleted");
                //do nothing
                adFinishedDoAction();
            }
        };
    }

    private void loadNewAd() {
        mRewardedAd.loadAd(
                getString(R.string.reward_ad_unit_id),
                new AdRequest.Builder().build()
        );
        //Log.d("AppDebug", "ad loading started");
    }

    private void adFinishedDoAction() {
        if (isShareClicked) {
            share();
            return;
        }

        finish();
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

    private void launchAdOrDoAction() {
        if (mRewardedAd != null && mRewardedAd.isLoaded()) {
            mRewardedAd.show();
        } else {
            adFinishedDoAction();
        }
    }

    @Override
    public void onResume() {
        mRewardedAd.resume(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        mRewardedAd.pause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mRewardedAd.destroy(this);
        super.onDestroy();
    }
}

