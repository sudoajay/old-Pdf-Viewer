package com.sudoajay.pdf_viewer.helperClass

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.sudoajay.pdf_viewer.databaseClasses.Database
import java.io.File
import java.util.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ScanPdf {
    val pdfPath = ArrayList<String>()
    private var mContext: Context? = null
    private var database: Database? = null
    fun scanFIle(mContext: Context?, externalDir: String?, sdCardDir: String?) {
        this.mContext = mContext
        database = Database(mContext)

        if (File(externalDir).exists()) {
            getAllPath(File(externalDir))
        }
        if (File(sdCardDir).exists()) {
            getAllPath(File(sdCardDir))
        }
        // Empty If the database have something
        if (!database!!.isEmpty) database!!.deleteData()
    }

    @SuppressLint("NewApi")
    private fun getAllPath(directory: File) {
        val extension = ".pdf"
        var getName: String
        try {
            for (child in directory.listFiles()) if (child.isDirectory) {
                Log.e("GotSomething",child.name)
                getAllPath(child)
            } else {
                getName = child.name
                if (getName.endsWith(extension)) pdfPath.add(child.absolutePath)
            }
        } catch (ignored: Exception) {
        }
    }

}