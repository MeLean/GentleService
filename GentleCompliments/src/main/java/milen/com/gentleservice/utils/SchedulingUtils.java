package milen.com.gentleservice.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import milen.com.gentleservice.R;
import milen.com.gentleservice.services.AlarmsProvider;

import java.util.Random;

public class SchedulingUtils {
    public static final int SCHEDULE = 1;
    public static final int SURPRISE = 2;

    public static final String TYPE_KEY = "type";
    public static final String PERIOD_KEY = "period";
    public static final String FIRE_AFTER_KEY = "waiting_key_fire";
    public static final String RANDOM_VALUE_KEY = "random_key_fire";

    private static final int ONE_DAY = 86400000; //default value one day



    public static void startComplimentingJob(Context context, Bundle extras) {
        AlarmsProvider.scheduleJob(context, extras);
    }

    public static int generateRandomMinutes(int num) {
        //min minutes are needed to ensure that
        //window for the periodic job is more then 15 minutes.
        int MIN_MINUTES = 15;
        int result;

        Random random = new Random();
        result = random.nextInt(num);

        return result > MIN_MINUTES ? result : MIN_MINUTES;
    }

    public static void stopComplimenting(Context context) {
        //Log.d("AppDebug", "SchedulingUtils stopComplimenting called");
        AlarmsProvider.cancelJob(context);
    }


    public static int calculateNextFireAfter(int period, int currentRandom, int nextRandom) {
        if (period > 0 && currentRandom > 0 && nextRandom > 0) {
            int timeUntilPeriodIsEnded = period - currentRandom;

            Log.d("AppDebug", " timeUntilPeriodIsEnded " + timeUntilPeriodIsEnded + "nextRandom " + nextRandom);
            int time = timeUntilPeriodIsEnded + nextRandom;
            return time > 0 ? time : (time * -1);
        }

        return ONE_DAY;
    }

    public static class InputValidator {
        public static String validate(Context context, String enteredInt) {
            //time constants should be in minutes
            int MINIMUM_WAITING_TIME = 60; // an hour
            int MAX_WAITING_TIME = 10080; //a week
            try {
                int inputNum = (Integer.parseInt(String.valueOf(enteredInt)));

                if (inputNum < MINIMUM_WAITING_TIME || inputNum > MAX_WAITING_TIME) {
                    if (inputNum < 0) {
                        //Just trow me to return error message!
                        throw new NumberFormatException("");
                    }

                    return context.getString(R.string.minimum_waiting_time_text) + MINIMUM_WAITING_TIME ;
                }
            } catch (NumberFormatException e) {
                return context.getString(R.string.invalid_number_text);
            }

            return null;
        }
    }

    public static Bundle makeNewExtras(Bundle extras) {
        if (extras == null){
            Log.d("AppDebug", "PersistableBundleCompat extras: is null");
            extras = new Bundle();
        }

        //if no value set default value
        int type = extras.getInt(TYPE_KEY, SURPRISE);
        int period = extras.getInt(PERIOD_KEY, ONE_DAY);

        int currentRandom = extras.getInt(RANDOM_VALUE_KEY, ONE_DAY);

        int nextRandom = generateRandomMinutes(period);

        int fireAfter;
        if (type == SURPRISE) {
            fireAfter = calculateNextFireAfter(period, currentRandom, nextRandom);
            extras.putInt(FIRE_AFTER_KEY, fireAfter);
            extras.putInt(RANDOM_VALUE_KEY, nextRandom);
        }

        return extras;
    }
}
