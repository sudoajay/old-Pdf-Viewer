package com.sudoajay.pdf_viewer.permission

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.documentfile.provider.DocumentFile
import com.sudoajay.pdf_viewer.R
import com.sudoajay.pdf_viewer.helperClass.CustomToast
import com.sudoajay.pdf_viewer.sharedPreference.ExternalPathSharedPreference
import com.sudoajay.pdf_viewer.sharedPreference.SdCardPathSharedPreference

@Suppress("ControlFlowWithEmptyBody", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AndroidExternalStoragePermission(private var context: Context, private var activity: Activity?) {
    private var externalSharedPreferences: ExternalPathSharedPreference? = null
    private var sdCardPathSharedPreference: SdCardPathSharedPreference? = null


    private fun storagePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            activity?.let {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        1)
            }
            //                if (ContextCompat.checkSelfPermission(context,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    int my_Permission_Request = 1;
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//
//                        ActivityCompat.requestPermissions(activity,
//                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, my_Permission_Request);
//                    } else {
//
//                        ActivityCompat.requestPermissions(activity,
//                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, my_Permission_Request);
//                    }
//                }
        } else {
        }
    }

    fun callThread() { // check if permission already given or not
        if (!isExternalStorageWritable) {
//             Its supports till android 9 & api 28
            if (Build.VERSION.SDK_INT <= 28) {
                val handler = Handler()
                handler.postDelayed({ callCustomPermissionDialog() }, 500)
            } else {
                val handler = Handler()
                handler.postDelayed({
                    CustomToast.toastIt(context, context.getString(R.string.errorMesExternal))

                    storageAccessFrameWork() }, 500)
            }
        }
    }

    private fun callCustomPermissionDialog() {
        val dialog = Dialog(context)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.activity_custom_dialog_permission)
        val buttonLearnMore = dialog.findViewById<Button>(R.id.see_More_button)
        val buttonContinue = dialog.findViewById<Button>(R.id.continue_Button)
        // if button is clicked, close the custom dialog
        buttonLearnMore.setOnClickListener {
            try {
                val url = "https://developer.android.com/training/permissions/requesting.html"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                activity?.startActivity(i)
            } catch (ignored: Exception) {
            }
        }
        buttonContinue.setOnClickListener {
            storagePermissionGranted()
            dialog.dismiss()
        }
        dialog.show()
    }

    val isExternalStorageWritable: Boolean
        get() {
            //
            return when {
//             Its supports till android 9 & api 28
                Build.VERSION.SDK_INT <= 28 -> {
                    val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                    val res = activity?.checkCallingOrSelfPermission(permission)
                    res == PackageManager.PERMISSION_GRANTED
                }

                else -> {
                    isSamePath ||
                            externalSharedPreferences!!.stringURI!!.isNotEmpty() && DocumentFile.fromTreeUri(context, Uri.parse(externalSharedPreferences!!.stringURI))!!.exists() && isSamePath

                }
            }

        }

    private val isSamePath: Boolean
        get() = externalSharedPreferences!!.externalPath!!.isNotEmpty() && getExternalPath(context).equals(externalSharedPreferences!!.externalPath)

    private fun storageAccessFrameWork() {
        try {
            val intent: Intent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                val requestCode = 58
                activity?.startActivityForResult(intent, requestCode)
            }
        } catch (e: Exception) {
            CustomToast.toastIt(context,context.getString(R.string.reportIt))

        }
    }


    init {
        externalSharedPreferences = ExternalPathSharedPreference(context)
        sdCardPathSharedPreference = SdCardPathSharedPreference(context)
    }

    companion object {
        fun getExternalPath(context: Context?): String? {
            //  Its supports till android 9
            val splitWord = "Android/data/"
            val cacheDir = (context!!.externalCacheDir?.absolutePath)?.split(splitWord)?.toTypedArray()
            return cacheDir?.get(0)

        }
    }


}