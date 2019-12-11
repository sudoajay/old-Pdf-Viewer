package com.sudoajay.pdf_viewer.permission

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.widget.Toast
import com.sudoajay.pdf_viewer.permission.AndroidExternalStoragePermission.Companion.getExternalPath
import com.sudoajay.pdf_viewer.sharedPreference.SdCardPathSharedPreference
import java.io.File

@SuppressLint("Registered")
class AndroidSdCardPermission {
    private var context: Context
    private var sdCardPathURL: String? = ""
    private var stringURI: String? = null
    private var sdCardPathSharedPreference: SdCardPathSharedPreference? = null
    private var activity: Activity? = null

    constructor(context: Context, activity: Activity?) {
        this.context = context
        this.activity = activity
        grab()
    }

    constructor(context: Context) {
        this.context = context
        grab()
    }

    fun callThread() {
        if (!isSdStorageWritable) {
            val handler = Handler()
            handler.postDelayed({ storageAccessFrameWork() }, 1800)
        }
    }

    private fun storageAccessFrameWork() {
        try {
            val intent: Intent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                val requestCode = 42
                activity!!.startActivityForResult(intent, requestCode)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "There is Error Please Report It", Toast.LENGTH_LONG).show()
        }
    }

    val isSdStorageWritable: Boolean
        get() = sdCardPathURL != getExternalPath(context) &&
                File(sdCardPathURL).exists() && File(sdCardPathURL).listFiles() != null

    private fun grab() { // gran the data from shared preference
        sdCardPathSharedPreference = SdCardPathSharedPreference(context)
        try {
            sdCardPathURL = sdCardPathSharedPreference!!.sdCardPath
            stringURI = sdCardPathSharedPreference!!.stringURI
        } catch (ignored: Exception) {
        }
    }

    fun getSdCardPathURL(): String? {
        return sdCardPathURL
    }

    fun setSdCardPathURL(sd_Card_Path_URL: String?) {
        this.sdCardPathURL = sd_Card_Path_URL
        sdCardPathSharedPreference!!.sdCardPath = sd_Card_Path_URL
    }

    fun setStringURI(string_URI: String?) {
        this.stringURI = string_URI
        sdCardPathSharedPreference!!.stringURI = string_URI
    }
}