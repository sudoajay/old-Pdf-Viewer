package com.sudoajay.pdf_viewer.sharedPreference

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.sudoajay.pdf_viewer.R

class SdCardPathSharedPreference constructor(context: Context) {
    // global variable
    private val editor: Editor
    private val context: Context
    private val pref: SharedPreferences = context.getSharedPreferences(context.getString(R.string.MY_PREFS_NAME), Context.MODE_PRIVATE)
    // send thd data to shared preferences
    var sdCardPath: String
        get() = pref.getString(context.getString(R.string.sdCardPath), "").toString()
        set(sdCardPath) { // send thd data to shared preferences
            editor.putString(context.getString(R.string.sdCardPath), sdCardPath)
            editor.apply()
        }

    var stringURI: String
        get() = pref.getString(context.getString(R.string.sdCardStringUri), "").toString()
        set(stringURI) {
            editor.putString(context.getString(R.string.sdCardStringUri), stringURI)
            editor.apply()
        }

    // constructor
    init {
        editor = pref.edit()
        editor.apply()
        this.context = context

    }
}