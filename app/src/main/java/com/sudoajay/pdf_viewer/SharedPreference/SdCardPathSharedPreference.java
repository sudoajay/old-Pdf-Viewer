package com.sudoajay.pdf_viewer.SharedPreference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;


import com.sudoajay.pdf_viewer.R;

import static android.content.Context.MODE_PRIVATE;

public class SdCardPathSharedPreference {

    // global varibale
    private SharedPreferences.Editor editor;
    private Context context;
    private SharedPreferences pref;


    // constructor
    @SuppressLint("CommitPrefEdits")
    public SdCardPathSharedPreference(Context context) {
        pref = context.getSharedPreferences(context.getString(R.string.MY_PREFS_NAME), MODE_PRIVATE);
        editor = pref.edit();
        this.context = context;

        // default value pass
        // grab the data from shared preference
        editor.putString(context.getString(R.string.sdCardPath), "");
        editor.putString(context.getString(R.string.stringUri), "");

    }

    public String getSdCardPath() {
        return pref.getString(context.getString(R.string.sdCardPath), "");
    }

    public void setSdCardPath(String sdCardPath) {

        // send thd data to shared preferences
        editor.putString(context.getString(R.string.sdCardPath), sdCardPath);
        editor.apply();
    }

    public String getStringURI() {
        return pref.getString(context.getString(R.string.stringUri), "");
    }

    public void setStringURI(String stringURI) {

        editor.putString(context.getString(R.string.stringUri), stringURI);
        editor.apply();
    }
}
