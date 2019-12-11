package com.sudoajay.pdf_viewer.sharedPreference

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.sudoajay.pdf_viewer.R

class PrefManager @SuppressLint("CommitPrefEdits") constructor(private val _context: Context) {
    private val pref: SharedPreferences
    private val editor: Editor

    var isFirstTimeLaunch: Boolean
        get() = pref.getBoolean(_context.getString(R.string.isFirstTimeLaunch), true)
        set(isFirstTime) {
            editor.putBoolean(_context.getString(R.string.isFirstTimeLaunch), isFirstTime)
            editor.apply()
        }

    init {
        // shared pref mode
        val privateMode = 0
        pref = _context.getSharedPreferences(_context.getString(R.string.MY_PREFS_NAME), privateMode)
        editor = pref.edit()
    }
}