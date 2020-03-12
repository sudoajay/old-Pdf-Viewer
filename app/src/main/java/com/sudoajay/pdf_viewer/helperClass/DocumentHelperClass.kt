package com.sudoajay.pdf_viewer.helperClass

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.sudoajay.pdf_viewer.permission.AndroidExternalStoragePermission
import com.sudoajay.pdf_viewer.sharedPreference.ExternalPathSharedPreference
import com.sudoajay.pdf_viewer.sharedPreference.SdCardPathSharedPreference

class DocumentHelperClass(private var context: Context) {
    private var externalPathSharedPreference: ExternalPathSharedPreference? = null
    private var sdCardPathSharedPreference: SdCardPathSharedPreference? = null

    fun separatePath(path: String): DocumentFile {

        val externalPath: String = AndroidExternalStoragePermission.getExternalPath(context).toString()
        var spilt: String
        val documentFile: DocumentFile
        if (path.contains(externalPath)) {
            documentFile = DocumentFile.fromTreeUri(context, Uri.parse(externalPathSharedPreference?.stringURI))!!
            spilt = externalPath
        } else {
            documentFile = DocumentFile.fromTreeUri(context, Uri.parse(sdCardPathSharedPreference?.stringURI))!!
            spilt = sdCardPathSharedPreference!!.sdCardPath
        }
        spilt = path.split(spilt)[1]
        return dugPath(spilt, documentFile)
    }

     private fun dugPath(path: String, document: DocumentFile): DocumentFile {
        var documentFile = document
        val list = path.split("/")
        for (file in list) {
            documentFile = documentFile.findFile(file)!!
        }
        return documentFile
    }

    init {
        externalPathSharedPreference = ExternalPathSharedPreference(context)
        sdCardPathSharedPreference = SdCardPathSharedPreference(context)
    }
}