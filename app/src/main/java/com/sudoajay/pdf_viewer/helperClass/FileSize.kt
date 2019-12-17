package com.sudoajay.pdf_viewer.helperClass

import java.text.DecimalFormat

object FileSize {
    @JvmStatic
    fun convertIt(size: Long): String {
        return when {
            size > 1024 * 1024 * 1024 -> { // GB
                getDecimal2Round(size.toDouble() / (1024 * 1024 * 1024)) + " GB"
            }
            size > 1024 * 1024 -> { // MB
                getDecimal2Round(size.toDouble() / (1024 * 1024)) + " MB"
            }
            else -> { // KB
                getDecimal2Round(size.toDouble() / 1024) + " KB"
            }
        }
    }

    private fun getDecimal2Round(time: Double): String {
        val df = DecimalFormat("#.#")
        return java.lang.Double.valueOf(df.format(time)).toString()
    }
}