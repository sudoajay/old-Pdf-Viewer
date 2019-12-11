@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "NAME_SHADOWING")

package com.sudoajay.pdf_viewer.helperClass

import android.content.Context
import android.net.Uri
import java.io.*

object CopyFile {
    @Throws(IOException::class)
    fun copy(src: File?, dst: File?) {
        val `in`: InputStream = FileInputStream(src)
        `in`.use { `in` ->
            val out: OutputStream = FileOutputStream(dst)
            out.use { out ->
                /* Transfer bytes from in to out */
                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            }
        }
    }

    fun copyUri(context: Context, sourceuri: Uri?, dst: File?) {
        try {
            val `in` = context.contentResolver.openInputStream(sourceuri!!)
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