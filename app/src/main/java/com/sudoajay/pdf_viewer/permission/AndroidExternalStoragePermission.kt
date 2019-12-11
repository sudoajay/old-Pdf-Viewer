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
import com.sudoajay.pdf_viewer.R

@Suppress("ControlFlowWithEmptyBody")
class AndroidExternalStoragePermission(private val context: Context, private val activity: Activity) {
    private fun storagePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1)
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
            val handler = Handler()
            handler.postDelayed({ callCustomPermissionDailog() }, 500)
        }
    }

    private fun callCustomPermissionDailog() {
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
                activity.startActivity(i)
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
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            val res = activity.checkCallingOrSelfPermission(permission)
            return res == PackageManager.PERMISSION_GRANTED
        }

    companion object {
        @JvmStatic
        fun getExternalPath(context: Context): String? {
            val splitWord = "Android/data/"
            val cacheDir = (context.externalCacheDir?.absolutePath)?.split(splitWord)?.toTypedArray()
            return cacheDir?.get(0)
        }
    }

}