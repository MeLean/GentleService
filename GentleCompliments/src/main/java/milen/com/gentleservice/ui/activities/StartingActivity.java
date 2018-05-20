package milen.com.gentleservice.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Date;
import java.util.Locale;

import milen.com.gentleservice.R;
import milen.com.gentleservice.api.database.DbLocaleTextChanger;
import milen.com.gentleservice.constants.ProjectConstants;
import milen.com.gentleservice.services.PhoenixService;
import milen.com.gentleservice.ui.adapters.MainViewPagerAdapter;
import milen.com.gentleservice.utils.AdManager;
import milen.com.gentleservice.utils.SchedulingUtils;
import milen.com.gentleservice.utils.SharedPreferencesUtils;

public class StartingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener {
    NavigationView mNavigationView;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ContextCompat.startForegroundService(this, new Intent(this, PhoenixService.class));

        if (getIntent() != null && getIntent().hasExtra(PhoenixService.REBIRTH_KEY)) {
            return;
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_starting);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkLocale();

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setItemIconTintList(null);

        mViewPager = findViewById(R.id.start_view_pager);
        mViewPager.setAdapter(new MainViewPagerAdapter(this));
        mViewPager.addOnPageChangeListener(this);
        //index 1 is SetupFragment
        mViewPager.setCurrentItem(1, false);

        if (AdManager.shouldLaunchAd(this)) {
            MobileAds.initialize(this, "pub-" + getString(R.string.ads_user_id));

            AdView mAdView = findViewById(R.id.ad_view);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void checkLocale() {
        String currentLocale =
                SharedPreferencesUtils.loadString(this, ProjectConstants.SAVED_LOCALE_KEY, null);
        if (currentLocale != null && !currentLocale.equalsIgnoreCase(Locale.getDefault().toString())){
            //locale has been changed
            DbLocaleTextChanger dbLocaleTextChanger = new DbLocaleTextChanger();
            dbLocaleTextChanger.execute(this);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_add) {
            //add compliment fragment is index 0
            mViewPager.setCurrentItem(0, true);
        } else if (id == R.id.nav_like_dislike) {
            //add like dislike fragment is index 2
            mViewPager.setCurrentItem(2, true);
        } else if (id == R.id.nav_settings) {
            //add like setup fragment is index 1
            mViewPager.setCurrentItem(1, true);
        } else if (id == R.id.nav_exit) {
            this.finish();
            System.exit(0);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        manageChosenPage(position);
    }

    @Override
    public void onPageSelected(int position) {
        manageChosenPage(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void manageChosenPage(int position) {
        mNavigationView.getMenu().getItem(position).setChecked(true);
        if(position == 0){
            setTitle(getString(R.string.title_activity_add_new_compliment));
        }else if(position == 1){
            setTitle(getString(R.string.app_name));
        } else if(position == 2){
            setTitle(getString(R.string.title_activity_like_hated));
        }else{
            setTitle(getString(R.string.app_name));
        }
    }

    public void showFireDate(View view) {
        Toast.makeText(StartingActivity.this,
                "should fire at:" +
                        new Date(
                                SharedPreferencesUtils.loadLong(StartingActivity.this,
                                        SchedulingUtils.SHOULD_FIRE_KEY,
                                        0)
                        ),
                Toast.LENGTH_SHORT).show();
    }
}
