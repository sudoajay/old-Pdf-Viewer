package com.sudoajay.pdf_viewer.permission

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.documentfile.provider.DocumentFile
import com.sudoajay.pdf_viewer.R
import com.sudoajay.pdf_viewer.helperClass.CustomToast
import com.sudoajay.pdf_viewer.sharedPreference.ExternalPathSharedPreference
import com.sudoajay.pdf_viewer.sharedPreference.SdCardPathSharedPreference


class AndroidExternalStoragePermission(private var context: Context, private var activity: Activity) {
    private var externalSharedPreferences: ExternalPathSharedPreference? = null
    private var sdCardPathSharedPreference: SdCardPathSharedPreference? = null


    fun callThread() { // check if permission already given or not
        if (!isExternalStorageWritable) {
            //  Here use of DocumentFile in android 10 not File is using anymore
            val handler = Handler()
            handler.postDelayed({
                if (Build.VERSION.SDK_INT <= 28) {
                    callPermissionDialog()
                } else {
                    CustomToast.toastIt(context, context.getString(R.string.selectExternalMes))

                    storageAccessFrameWork()
                }
            }, 500)
        }
    }

    private fun callPermissionDialog() {
        AlertDialog.Builder(context)
                .setIcon(R.drawable.internal_storage_icon)
                .setTitle(context.getString(R.string.activity_custom_dialog_permission_TextView1))
                .setMessage(context.getString(R.string.activity_custom_dialog_permission_TextView2))
                .setCancelable(true)
                .setPositiveButton(R.string.continueButton) { _, _ ->
                    storagePermissionGranted()
                }
                .setNegativeButton(R.string.readMoreButton) { _, _ ->
                    try {
                        val url = "https://developer.android.com/training/permissions/requesting.html"
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(url)
                        activity.startActivity(i)
                    } catch (ignored: Exception) {
                    }
                }
                .show()
    }

    private fun storagePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            activity.let {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        1)
            }
        }
    }

    private fun storageAccessFrameWork() {
        try {
            val intent: Intent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                val requestCode = 58
                activity.startActivityForResult(intent, requestCode)
            }
        } catch (e: Exception) {
            CustomToast.toastIt(context, context.getString(R.string.reportIt))

        }
    }

    val isExternalStorageWritable: Boolean
        get() {
            //
            return when {
                //  Here use of DocumentFile in android 10 not File is using anymore
                Build.VERSION.SDK_INT <= 28 -> {

                    if (Build.VERSION.SDK_INT <= 22) {
                        val externalPathSharedPreference = ExternalPathSharedPreference(context)
                        externalPathSharedPreference.externalPath = getExternalPath(context).toString()
                    }
                    val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                    val res = activity.checkCallingOrSelfPermission(permission)
                    res == PackageManager.PERMISSION_GRANTED
                }

                else -> {
                    isSamePath ||
                            externalSharedPreferences!!.stringURI.isNotEmpty() && DocumentFile.fromTreeUri(context, Uri.parse(externalSharedPreferences!!.stringURI))!!.exists() && isSamePath
                }
            }

        }

    private val isSamePath: Boolean
        get() = externalSharedPreferences!!.stringURI.isNotEmpty() && getExternalPath(context).equals(externalSharedPreferences!!.externalPath)



    init {
        externalSharedPreferences = ExternalPathSharedPreference(context)
        sdCardPathSharedPreference = SdCardPathSharedPreference(context)
    }

    companion object {
        fun getExternalPath(context: Context?): String? {
            //  Its supports till android 9
            val splitWord = "Android/data/"
            val cacheDir = (context!!.externalCacheDir?.absolutePath)?.split(splitWord)
            return cacheDir?.get(0)

        }
    }
}