package milen.com.gentleservice.utils;

import android.content.Context;
import android.util.Log;

import com.evernote.android.job.util.support.PersistableBundleCompat;

import milen.com.gentleservice.R;
import milen.com.gentleservice.services.evernote_job.AlarmsProvider;

import java.util.Random;

public class SchedulingUtils {
    public static final int SCHEDULE = 1;
    public static final int SURPRISE = 2;

    public static final String TYPE_KEY = "type";
    public static final String PERIOD_KEY = "period";
    public static final String FIRE_AFTER_KEY = "waiting_key_fire";

    private static final int ONE_DAY = 86400000; //default value one day

    public  static int currentJobId;

    public static void startComplimentingJob(PersistableBundleCompat extras) {
        currentJobId = AlarmsProvider.scheduleExactJob(extras);
        Log.d("AppDebug", "currentJobId: " + currentJobId);
    }

    public static int generateRandom(int num) {
        if (num != 0) {
            Random random = new Random();
            return random.nextInt(num);
        }

        return num;
    }

    public static void stopComplimenting() {
        //Log.d("AppDebug", "SchedulingUtils stopComplimenting called");
        AlarmsProvider.cancelJob(currentJobId);
        currentJobId = -1;
    }


    private static int calculateNextFireAfter(int period, int fireAfter) {
        if (period > 0 && fireAfter > 0) {
            int nextFireAfter = generateRandom(period);
            int timeUntilPeriodIsEnded = period - fireAfter;

            Log.d("AppDebug", "nextFireAfter " + nextFireAfter + " timeUntilPeriodIsEnded " + timeUntilPeriodIsEnded);
            int time = timeUntilPeriodIsEnded + nextFireAfter;
            return time > 0 ? time : (time * -1);
        }

        return ONE_DAY;
    }

    public static class InputValidator {
        public static String validate(Context context, String enteredInt) {
            //time constants should be in minutes
            int MINIMUM_WAITING_TIME = 1; //todo one hour in minutes is a minimum time
            int MAX_WAITING_TIME = 35790;
            try {
                int inputNum = (Integer.parseInt(String.valueOf(enteredInt)));

                if (inputNum < MINIMUM_WAITING_TIME || inputNum > MAX_WAITING_TIME) {
                    if (inputNum < 0) {
                        //Just trow me to return error message!
                        throw new NumberFormatException("");
                    }

                    return context.getString(R.string.minimum_waiting_time_text) + MINIMUM_WAITING_TIME;
                }
            } catch (NumberFormatException e) {
                return context.getString(R.string.invalid_number_text);
            }

            return null;
        }
    }

    public static PersistableBundleCompat makeNewExtras(PersistableBundleCompat extras) {
        if (extras == null){
            Log.d("AppDebug", "PersistableBundleCompat extras: is null");
            extras = new PersistableBundleCompat();
        }

        //if no value set default value
        int type = extras.getInt(TYPE_KEY, SURPRISE);
        int period = extras.getInt(PERIOD_KEY, ONE_DAY);
        int fireAfter =  extras.getInt(FIRE_AFTER_KEY, ONE_DAY);


        if (type == SURPRISE) {
            fireAfter = calculateNextFireAfter(period, fireAfter);
            extras.putInt(FIRE_AFTER_KEY, fireAfter);
        }

        return extras;
    }
}
