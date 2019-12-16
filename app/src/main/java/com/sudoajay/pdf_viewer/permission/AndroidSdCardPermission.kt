package com.sudoajay.pdf_viewer.permission

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import com.sudoajay.pdf_viewer.R
import com.sudoajay.pdf_viewer.helperClass.CustomToast
import com.sudoajay.pdf_viewer.sharedPreference.ExternalPathSharedPreference
import com.sudoajay.pdf_viewer.sharedPreference.SdCardPathSharedPreference
import java.io.File

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SuppressLint("Registered")
class AndroidSdCardPermission {
    private var context: Context
    private var activity: Activity? = null
    private var sdCardPathURL: String? = ""
    private var stringURI: String? = null
    private var sdCardPathSharedPreference: SdCardPathSharedPreference? = null
    private var externalSharedPreferences :ExternalPathSharedPreference? =null


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
                CustomToast.toastIt(context, context.getString(R.string.errorMesSdCard))
                storageAccessFrameWork()
            }, 500)
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
        get() {
            return when {
                Build.VERSION.SDK_INT <= 22 ->{
                     File(sdCardPathURL).exists() || isSameUri
                }
                else -> ( sdCardPathSharedPreference!!.stringURI!!.isNotEmpty()
                        && DocumentFile.fromTreeUri(context, Uri.parse(sdCardPathSharedPreference!!.stringURI))!!.exists())
                        || isSameUri
            }
        }

    private val isSameUri
        get()= externalSharedPreferences!!.stringURI!!.isNotEmpty() && sdCardPathSharedPreference!!.stringURI!!.isNotEmpty() &&
                !externalSharedPreferences!!.stringURI.equals(sdCardPathSharedPreference!!.stringURI)


    private fun grab() { // gran the data from shared preference
        sdCardPathSharedPreference = SdCardPathSharedPreference(context)
        externalSharedPreferences = ExternalPathSharedPreference(context)

        try {
            sdCardPathURL = sdCardPathSharedPreference!!.sdCardPath
            stringURI = sdCardPathSharedPreference!!.stringURI

        } catch (ignored: Exception) {
        }
    }

    fun getSdCardPathURL(): String? {
        return sdCardPathSharedPreference!!.sdCardPath
    }

}