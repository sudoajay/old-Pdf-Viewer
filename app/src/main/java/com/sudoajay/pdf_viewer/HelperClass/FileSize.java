package com.sudoajay.pdf_viewer.HelperClass;

import java.text.DecimalFormat;

public class FileSize {

    public static String Convert_It(long size) {
        if (size > (1024 * 1024 * 1024)) {
            // GB
            return GetDecimal2Round((double) size / (1024 * 1024 * 1024)) + " GB";
        } else if (size > (1024 * 1024)) {
            // MB
            return GetDecimal2Round((double) size / (1024 * 1024)) + " MB";

        } else {
            // KB
            return GetDecimal2Round((double) size / (1024)) + " KB";
        }

    }


    private static String GetDecimal2Round(double time){
        DecimalFormat df = new DecimalFormat("#.#");
        return Double.valueOf(df.format(time)).toString();
    }
}
