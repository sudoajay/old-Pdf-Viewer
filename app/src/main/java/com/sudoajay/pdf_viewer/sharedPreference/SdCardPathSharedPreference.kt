package com.sudoajay.pdf_viewer.sharedPreference

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.sudoajay.pdf_viewer.R

class SdCardPathSharedPreference @SuppressLint("CommitPrefEdits") constructor(context: Context) {
    // global varibale
    private val editor: Editor
    private val context: Context
    private val pref: SharedPreferences = context.getSharedPreferences(context.getString(R.string.MY_PREFS_NAME), Context.MODE_PRIVATE)
    // send thd data to shared preferences
    var sdCardPath: String?
        get() = pref.getString(context.getString(R.string.sdCardPath), "")
        set(sdCardPath) { // send thd data to shared preferences
            editor.putString(context.getString(R.string.sdCardPath), sdCardPath)
            editor.apply()
        }

    var stringURI: String?
        get() = pref.getString(context.getString(R.string.stringUri), "")
        set(stringURI) {
            editor.putString(context.getString(R.string.stringUri), stringURI)
            editor.apply()
        }

    // constructor
    init {
        editor = pref.edit()
        this.context = context
        // default value pass
// grab the data from shared preference
        editor.putString(context.getString(R.string.sdCardPath), "")
        editor.putString(context.getString(R.string.stringUri), "")
    }
}