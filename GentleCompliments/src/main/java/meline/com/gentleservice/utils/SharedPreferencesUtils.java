package meline.com.gentleservice.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreferencesUtils {
    private static final String PREF_NAME = "StarAdvice.SharedPreferences";

    public static void saveString(Context context, String key, String value) {
        getSharedPreferences(context, PREF_NAME)
                .edit()
                .putString(key, value)
                .apply();

    }

    public static void saveInt(Context context, String key, int value) {
        getSharedPreferences(context, PREF_NAME)
                .edit()
                .putInt(key, value)
                .apply();
    }

    public static  void saveLong(Context context, String key, long value) {
        getSharedPreferences(context, PREF_NAME)
                .edit()
                .putLong(key, value)
                .apply();
    }

    public static void saveBoolean(Context context,String key, boolean value) {
        getSharedPreferences(context, PREF_NAME)
                .edit()
                .putBoolean(key, value)
                .apply();
    }

    /*public static void saveStringSet(Context context, String key, Set<String> value) {
        getSharedPreferences(context, PREF_NAME)
                .edit()
                .putStringSet(key, value)
                .apply();
    }*/

    public static String loadString(Context context, String key, String defValue) {
        return getSharedPreferences(context, PREF_NAME).getString(key, defValue);
    }

    static int loadInt(Context context, String key, int defValue) {
        return getSharedPreferences(context, PREF_NAME).getInt(key, defValue);
    }

    public  static long loadLong(Context context, String key, long defValue) {
        return getSharedPreferences(context, PREF_NAME).getLong(key, defValue);
    }

    public static  boolean loadBoolean(Context context, String key, boolean defValue) {
        return getSharedPreferences(context, PREF_NAME).getBoolean(key, defValue);
    }

    public static void removeValue(Context context, String key) {
        getSharedPreferences(context, PREF_NAME)
                .edit()
                .remove(key)
                .apply();
    }

  /*  public static Set<String> loadStringSet(Context context, String key, Set<String> defValue) {
        return getSharedPreferences(context, PREF_NAME).getStringSet(key, defValue);
    }*/

    /*public void clearSharedPreferences() {
         getSharedPreferences(context, PREF_NAME)
                .edit()
                .clear()
                .apply();
    }*/

    private static SharedPreferences getSharedPreferences(Context context, String prefName) {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }
}