package com.meline.gentleservice.utils;

import android.content.Context;

public class AdManager {
    private static final String SAVED_AD_MILLISECONDS_KEY = "nextAdMilliseconds";
    private static final long ONE_WEEK_MILLISECONDS = 604800000;

    public static boolean shouldLaunchAd(Context context) {
        return SharedPreferencesUtils.loadLong(context, SAVED_AD_MILLISECONDS_KEY, 0)
                <= System.currentTimeMillis();
    }

    public static void reward(Context context) {
        SharedPreferencesUtils.saveLong(context,
                SAVED_AD_MILLISECONDS_KEY, System.currentTimeMillis() + ONE_WEEK_MILLISECONDS);
    }
}
