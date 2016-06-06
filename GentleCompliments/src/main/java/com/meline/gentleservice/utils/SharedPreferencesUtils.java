package com.meline.gentleservice.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.meline.gentleservice.ui.interfaces.LocaleLoader;
import com.meline.gentleservice.ui.interfaces.LocaleSaver;

public class SharedPreferencesUtils implements LocaleLoader, LocaleSaver {
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

    /*public void clearSharedPreferences() {
        spEditor.clear();
        spEditor.commit();
    }*/
}

