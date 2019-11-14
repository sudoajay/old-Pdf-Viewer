package com.sudoajay.pdf_viewer.SharedPreference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.sudoajay.pdf_viewer.R;

public class PrefManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;


    @SuppressLint("CommitPrefEdits")
    public PrefManager(Context context) {
        this._context = context;
        // shared pref mode
        int PRIVATE_MODE = 0;
        pref = _context.getSharedPreferences(context.getString(R.string.MY_PREFS_NAME), PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(_context.getString(R.string.isFirstTimeLaunch), isFirstTime);
        editor.apply();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(_context.getString(R.string.isFirstTimeLaunch), true);
    }

}
