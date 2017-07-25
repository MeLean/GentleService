package com.meline.gentleservice.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.meline.gentleservice.ui.interfaces.LocaleLoader;
import com.meline.gentleservice.ui.interfaces.LocaleSaver;

import java.util.Set;

public class SharedPreferencesUtils implements LocaleLoader, LocaleSaver {
    private static final String PREF_NAME = "com.mddimitrov.staradvice.utils.SharedPreferences";

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

    public  static int loadInt(Context context, String key, int defValue) {
        return getSharedPreferences(context, PREF_NAME).getInt(key, defValue);
    }

    public  static long loadLong(Context context, String key, long defValue) {
        return getSharedPreferences(context, PREF_NAME).getLong(key, defValue);
    }

    public static  boolean loadBoolean(Context context, String key, boolean defValue) {
        return getSharedPreferences(context, PREF_NAME).getBoolean(key, defValue);
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



    //todo delete this

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    public SharedPreferencesUtils(Context context, String name) {
        this.sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        this.spEditor = this.sharedPreferences.edit();
    }

    public void putStringInSharedPreferences(String key, String value) {
        spEditor.putString(key, value);
        spEditor.commit();
    }

    /*public void putIntInSharedPreferences(String key, int value) {
        spEditor.putInt(key, value);
        spEditor.commit();
    }*/

    public void putLongInSharedPreferences(String key, long value) {
        spEditor.putLong(key, value);
        spEditor.commit();
    }

    public void putBooleanInSharedPreferences(String key, boolean value) {
        spEditor.putBoolean(key, value);
        spEditor.commit();
    }

    public String getStringFromSharedPreferences(String key) {
        return sharedPreferences.getString(key, null);
    }

    /*public int getIntFromSharedPreferences(String key) {
        return sharedPreferences.getInt(key, 0);
    }*/

    public long getLongFromSharedPreferences(String key) {
        return sharedPreferences.getLong(key, 0);
    }

    public boolean getBooleanFromSharedPreferences(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    @Override
    public String loadLocale(String key) {
        return sharedPreferences.getString(key, null);
    }

    @Override
    public void saveLocale(String key, String localeLanguage) {
        this.putStringInSharedPreferences(key, localeLanguage);
    }

}