package com.sudoajay.pdf_viewer.helperClass

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.sudoajay.pdf_viewer.permission.AndroidExternalStoragePermission
import com.sudoajay.pdf_viewer.sharedPreference.ExternalPathSharedPreference
import com.sudoajay.pdf_viewer.sharedPreference.SdCardPathSharedPreference
import java.io.File
import java.util.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ScanPdf {
    val pdfPath = ArrayList<String>()
    private var mContext: Context? = null
    private var externalSharedPreference: ExternalPathSharedPreference? = null
    private var sdCardSharedPreference: SdCardPathSharedPreference? = null
    private var isAndroidDir: Boolean? = null
    private var parentName: String? = null
    fun scanFIle(mContext: Context?, externalDir: String?, sdCardDir: String?) {
        this.mContext = mContext
//      Its supports till android 9 & api 28
        if (File(externalDir).exists()) {
            getAllPathFile(File(externalDir))
        }
        if (File(sdCardDir).exists()) {
            getAllPathFile(File(sdCardDir))
        }
    }

    fun scanFile(mContext: Context?) {
        this.mContext = mContext
        externalSharedPreference = ExternalPathSharedPreference(mContext!!)
        sdCardSharedPreference = SdCardPathSharedPreference(mContext)
        var documentFile: DocumentFile?

        if (isSamePath) {
            isAndroidDir = false
            documentFile = DocumentFile.fromTreeUri(mContext, Uri.parse(externalSharedPreference!!.stringURI))
            parentName = documentFile!!.name
            getAllPathDocumentFile(documentFile)
        }

        if (!isSameUri) {
            isAndroidDir = false
            documentFile = DocumentFile.fromTreeUri(mContext, Uri.parse(sdCardSharedPreference!!.stringURI))
            parentName = documentFile!!.name
            getAllPathDocumentFile(documentFile)
        }

    }


    private fun getAllPathFile(directory: File) {
        val extension = ".pdf"
        var getName: String
        try {
            for (child in directory.listFiles())
                if (child.isDirectory) getAllPathFile(child)
                else {
                getName = child.name
                if (getName.endsWith(extension)) pdfPath.add(child.absolutePath)
                }
        } catch (ignored: Exception) {
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

    private val isSamePath: Boolean
        get() = AndroidExternalStoragePermission.getExternalPath(mContext).equals(externalSharedPreference!!.externalPath)

    private val isSameUri
        get() =externalSharedPreference!!.stringURI.equals(sdCardSharedPreference!!.stringURI)


}