package com.sudoajay.pdf_viewer.helperClass

import android.content.Context
import android.net.Uri
import java.io.*

object CopyFile {
    fun copy(s: File, d: File) {
        val `in`: InputStream = FileInputStream(s)
        `in`.use { inputStream ->
            val out: OutputStream = FileOutputStream(d)
            out.use { outputStream ->
                /* Transfer bytes from `inputStream` to outputStream */
                val buf = ByteArray(1024)
                var len: Int
                while (inputStream.read(buf).also { len = it } > 0) {
                    outputStream.write(buf, 0, len)
                }
            }
        }
    }

    fun copyUri(context: Context, sourceUri: Uri, dst: File) {
        try {
            val `in` = context.contentResolver.openInputStream(sourceUri)
            val out: OutputStream?
            out = FileOutputStream(dst)
            val buf = ByteArray(1024)
            var len: Int
            assert(`in` != null)
            while (`in`!!.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
            out.close()
            `in`.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}