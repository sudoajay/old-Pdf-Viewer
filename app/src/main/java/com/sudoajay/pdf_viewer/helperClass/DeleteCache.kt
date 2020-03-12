package com.sudoajay.pdf_viewer.helperClass

import android.content.Context
import java.io.File

internal object DeleteCache {
    fun deleteCache(context: Context) {
        try {

//            if (Build.VERSION.SDK_INT <= 28) {
                val dir = context.cacheDir
                deleteWithFile(dir)
//            } else {
//                val cacheDir = context.cacheDir.absolutePath
//                val documentHelperClass = DocumentHelperClass(context)
//                val documentFile = documentHelperClass.separatePath(cacheDir)
//                deleteWithDoc(documentFile)
//            }
            CustomToast.toastIt(context, "Successfully Cache Data Is Deleted")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteWithFile(dir: File): Boolean {
        return when {
            dir.isDirectory -> {
                val children = dir.listFiles()
                for (i in children!!.indices) {
                    deleteWithFile(children[i])
                }
                dir.delete()
            }
            dir.isFile -> {
                dir.delete()
            }
            else -> {
                return false
            }
        }
    }

//    private fun deleteWithDoc(documentFile: DocumentFile) {
//        if (documentFile.isDirectory) {
//            for (file in documentFile.listFiles()) {
//                deleteWithDoc(file)
//            }
//            if (documentFile.listFiles().isEmpty())
//                documentFile.delete()
//
//        } else {
//            documentFile.delete()
//        }
//    }
}