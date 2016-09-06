package com.meline.gentleservice.utils;

import android.content.Context;

import com.meline.gentleservice.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

//static class
public final class  CalendarUtils {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy MM dd HH:mm:ss Z");

    private CalendarUtils() {
    }

    public static String stringifyDateInFormat(Date date){
        return DATE_FORMAT.format(date);
    }

    public static long minutesToMilliseconds(long minutes){
        return minutes * 1000 * 60;
    }

    public static long millisecondsToMinutes(long milliseconds){
        return milliseconds / 1000 / 60;
    }

    public static long getMillisecondsFromTime(int hours, int minutes){
        return 60 * minutesToMilliseconds(hours) + minutesToMilliseconds(minutes);
    }

    public static boolean checkIsBetween(long startMilliseconds, long currentMilliseconds, long endMilliseconds){
        boolean isBetween;
        long startTime = startMilliseconds;
        long currentTime = currentMilliseconds;
        long endTime = endMilliseconds;

        if (currentMilliseconds <= endMilliseconds){
            currentTime = addDayInMilliseconds(currentTime);
        }

        if (startTime < endTime){
            startTime = addDayInMilliseconds(startTime);
        }

        if (currentTime < startTime) {
            isBetween = false;
        } else {
            if (currentTime > endTime) {
                endTime = addDayInMilliseconds(endMilliseconds);
            }

            isBetween = currentTime < endTime;
        }

        return isBetween;
    }

    static long addDayInMilliseconds(long currentMilliseconds){
        return currentMilliseconds + (24 * 60 * 60 * 1000); //(one day) hours * minutes * seconds * milliseconds
    }

    public static long manageWaitingTime(Context context) {
        long nextFireInMilliseconds;
        SharedPreferencesUtils spUtils = new SharedPreferencesUtils(context, context.getString(R.string.sp_name));
        long timeWaitPeriod = spUtils.getLongFromSharedPreferences(context.getString(R.string.sp_timeWaitingPeriod));
        long oldEndPeriod = spUtils.getLongFromSharedPreferences(context.getString(R.string.sp_timeEndPeriod));
        long newEndPeriod = oldEndPeriod + timeWaitPeriod;
        Random random = new Random();
        nextFireInMilliseconds = oldEndPeriod + (long) (random.nextDouble() * timeWaitPeriod);
        spUtils.putLongInSharedPreferences(context.getString(R.string.sp_timeEndPeriod), newEndPeriod);
        spUtils.putLongInSharedPreferences(context.getString(R.string.sp_fireNextInMilliseconds), nextFireInMilliseconds);
        return nextFireInMilliseconds;
    }
}
