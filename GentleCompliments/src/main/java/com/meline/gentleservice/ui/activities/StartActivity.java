package com.meline.gentleservice.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.meline.gentleservice.ProjectConstants;
import com.meline.gentleservice.R;
import com.meline.gentleservice.ui.adapters.MainViewPagerAdapter;
import com.meline.gentleservice.utils.SharedPreferencesUtils;

public class StartActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener {
    NavigationView mNavigationView;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mViewPager = (ViewPager) findViewById(R.id.start_view_pager);
        mViewPager.setAdapter(new MainViewPagerAdapter(this));
        mViewPager.addOnPageChangeListener(this);
        //index 1 is SetupFragment
        mViewPager.setCurrentItem(1, false);


        MobileAds.initialize(this, "pub-" + getString(R.string.ads_user_id));

        AdView mAdView = (AdView) findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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


}
