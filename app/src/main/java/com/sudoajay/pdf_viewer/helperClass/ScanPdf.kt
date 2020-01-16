package com.sudoajay.pdf_viewer.helperClass

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import com.sudoajay.pdf_viewer.permission.AndroidExternalStoragePermission
import com.sudoajay.pdf_viewer.permission.AndroidSdCardPermission
import com.sudoajay.pdf_viewer.sharedPreference.ExternalPathSharedPreference
import com.sudoajay.pdf_viewer.sharedPreference.SdCardPathSharedPreference
import java.io.File
import java.util.*

class ScanPdf(private val mContext: Context, activity: Activity) {
    val pdfPath = ArrayList<String>()

    private val externalSharedPreference = ExternalPathSharedPreference(mContext)
    private val sdCardSharedPreference = SdCardPathSharedPreference(mContext)
    private val androidSdCardPermission = AndroidSdCardPermission(mContext)
    private val androidExternalStoragePermission = AndroidExternalStoragePermission(mContext, activity)
    private var isAndroidDir: Boolean? = null
    private var parentName: String? = null

    fun scanUsingFile() {

        val externalDir = externalSharedPreference.externalPath
        val sdCardDir = sdCardSharedPreference.sdCardPath
//      Its supports till android 9 & api 28
        if (androidExternalStoragePermission.isExternalStorageWritable) {
            getAllPathFile(File(externalDir))
        }
        if (Build.VERSION.SDK_INT >= 21 && androidSdCardPermission.isSdStorageWritable) {
            getAllPathFile(File(sdCardDir))
        }
    }

    private fun getAllPathFile(directory: File) {
        val extension = ".pdf"
        var getName: String
        try {
            for (child in directory.listFiles()!!)
                if (child.isDirectory) getAllPathFile(child)
                else {
                    getName = child.name
                    if (getName.endsWith(extension))
                        pdfPath.add(child.absolutePath)
                }
        } catch (ignored: Exception) {
        }
    }

    fun scanUsingDoc() {

        var documentFile: DocumentFile
        val externalUri = externalSharedPreference.stringURI
        val sdCardUri = sdCardSharedPreference.stringURI

        if (androidExternalStoragePermission.isExternalStorageWritable) {
            isAndroidDir = false
            documentFile = DocumentFile.fromTreeUri(mContext, Uri.parse(externalUri))!!
            parentName = documentFile.name
            getAllPathDocumentFile(documentFile)
        }

        if (androidSdCardPermission.isSdStorageWritable) {
            isAndroidDir = false
            documentFile = DocumentFile.fromTreeUri(mContext, Uri.parse(sdCardUri))!!
            parentName = documentFile.name
            getAllPathDocumentFile(documentFile)
        }

    }

    private fun getAllPathDocumentFile(directory: DocumentFile) {
        val extension = ".pdf"
        var getName: String
        try {
            for (child in directory.listFiles())
                if (child.isDirectory) {
                    if (!isAndroidDir!!) {
                        if (child.name!! == "Android" && child.parentFile!!.name.equals(parentName)) {
                            isAndroidDir = true
                            continue
                        }
                    }
                    getAllPathDocumentFile(child)
                } else {
                    getName = child.name.toString()
                    if (getName.endsWith(extension)) {
                        pdfPath.add(child.uri.toString())
                    }
                }
        } catch (ignored: Exception) {
        }
    }

}