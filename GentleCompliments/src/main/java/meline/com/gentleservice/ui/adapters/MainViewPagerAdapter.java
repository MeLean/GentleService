package meline.com.gentleservice.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;

import meline.com.gentleservice.R;
import meline.com.gentleservice.ui.fragments.AddNewComplimentFragment;
import meline.com.gentleservice.ui.fragments.ComplimentSetupFragment;
import meline.com.gentleservice.ui.fragments.LikeHatedComplimentFragment;


public class MainViewPagerAdapter extends FragmentPagerAdapter {
    private static int mItemsCount;
    private final FragmentManager mFragmentManager;

    public MainViewPagerAdapter(AppCompatActivity activity) {
        super(activity.getSupportFragmentManager());
        this.mFragmentManager = activity.getSupportFragmentManager();
        mItemsCount = 3; //there are three fragments
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = mFragmentManager.findFragmentByTag("android:switcher:" + R.id.start_view_pager + ":" + position);
                return fragment != null ? fragment : new AddNewComplimentFragment();
            case 1:
                fragment = mFragmentManager.findFragmentByTag("android:switcher:" + R.id.start_view_pager + ":" + position);
                return fragment != null ? fragment : new ComplimentSetupFragment();
            case 2:
                fragment = mFragmentManager.findFragmentByTag("android:switcher:" + R.id.start_view_pager + ":" + position);
                return fragment != null ? fragment : new LikeHatedComplimentFragment();
            default:
                throw new UnsupportedOperationException("Nod valid position: " + position);
        }
    }

    @Override
    public int getCount() {
        return mItemsCount;
    }
}
