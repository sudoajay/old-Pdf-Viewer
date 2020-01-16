package com.sudoajay.pdf_viewer.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.widget.Toast
import com.sudoajay.pdf_viewer.R
import com.sudoajay.pdf_viewer.helperClass.CustomToast
import com.sudoajay.pdf_viewer.sharedPreference.ExternalPathSharedPreference
import com.sudoajay.pdf_viewer.sharedPreference.SdCardPathSharedPreference
import java.io.File

class AndroidSdCardPermission {
    private var context: Context
    private var activity: Activity? = null
    private var sdCardPathURL: String? = ""
    private var stringURI: String? = null
    private var sdCardPathSharedPreference: SdCardPathSharedPreference? = null
    private var externalSharedPreferences: ExternalPathSharedPreference? = null

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
            handler.postDelayed({
                // Its Support from Lollipop
                if (Build.VERSION.SDK_INT >= 21) {
                    CustomToast.toastIt(context, context.getString(R.string.selectSdCardMes))
                    storageAccessFrameWork()
                } else {
                    CustomToast.toastIt(context, context.getString(R.string.supportAboveSdCard))
                }
            }, 500)
        }
    }

    private fun storageAccessFrameWork() {
        try {
            val intent: Intent
            // Its Support from Lollipop
            if (Build.VERSION.SDK_INT >= 21) {
                intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                val requestCode = 42
                activity!!.startActivityForResult(intent, requestCode)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "There is Error Please Report It", Toast.LENGTH_LONG).show()
        }
    }

    val isSdStorageWritable: Boolean
        get() {
            return when {
                //  Here use of DocumentFile in android 10 not File is using anymore
                Build.VERSION.SDK_INT > 28 -> sdCardPathSharedPreference!!.stringURI.isNotEmpty()
                        && sdCardPathSharedPreference!!.sdCardPath != AndroidExternalStoragePermission.getExternalPath(context)
                        && !isSameUri
                Build.VERSION.SDK_INT >= 21 -> File(sdCardPathSharedPreference!!.sdCardPath).exists()
                        && sdCardPathSharedPreference!!.sdCardPath != AndroidExternalStoragePermission.getExternalPath(context)
                else -> {
                    true
                }
            }
        }

    private val isSameUri
        get() = externalSharedPreferences!!.stringURI == sdCardPathSharedPreference!!.stringURI


    private fun grab() { // gran the data from shared preference
        sdCardPathSharedPreference = SdCardPathSharedPreference(context)
        externalSharedPreferences = ExternalPathSharedPreference(context)

        try {
            sdCardPathURL = sdCardPathSharedPreference!!.sdCardPath
            stringURI = sdCardPathSharedPreference!!.stringURI

        } catch (ignored: Exception) {
        }
    }
}

