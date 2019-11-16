package com.sudoajay.pdf_viewer.HelperClass;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;

public class ScanPdf {
    private ArrayList<String> pdfPath = new ArrayList<>();
    private Context mContext;


    public void Duplication(final Context mContext, final String external_dir, final String sd_Card_dir) {

        this.mContext = mContext;
        if (new File(external_dir).exists()) {
            GetAllPath(new File(external_dir));
        }
        if (new File(sd_Card_dir).exists()) {
            GetAllPath(new File(sd_Card_dir));
        }

    }

    private void GetAllPath(final File directory) {
        try {
            String getName, getExt;
            for (File child : directory.listFiles())
                if (child.isDirectory() && !child.equals(mContext.getExternalCacheDir())) {
                    GetAllPath(child);
                } else {
                    getName = child.getName();
                    getExt = getExtension(getName);
                    if (getExt.equals("pdf"))
                        pdfPath.add(child.getAbsolutePath());
                }
        } catch (Exception ignored) {

        }
    }

    private String getExtension(final String path) {
        int i = path.lastIndexOf('.');
        String extension = "";
        if (i > 0) {
            extension = path.substring(i + 1);
        }
        return extension;
    }

    public ArrayList<String> getPdfPath() {
        return pdfPath;
    }
}
