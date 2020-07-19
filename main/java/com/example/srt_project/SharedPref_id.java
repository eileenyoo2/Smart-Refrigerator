package com.example.srt_project;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref_id {
    public static final String PREFERENCES_NAME = "myid_preference";
    private static final String DEFAULT_VALUE_STRING = "";
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor editor;
    private static SharedPreferences getPreferences(Context context) {

        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

    }

    public static void setString(Context context, String key, String value) {

        prefs = getPreferences(context);

        editor = prefs.edit();

        editor.putString(key, value);

        editor.commit();

    }

    public static String getString(Context context, String key) {

        prefs = getPreferences(context);

        String value = prefs.getString(key, DEFAULT_VALUE_STRING);

        return value;

    }

    public static void clear(Context context) {

        prefs = getPreferences(context);

        editor = prefs.edit();

        editor.clear();

        editor.commit();

    }
}
