package com.sudoajay.pdf_viewer.HelperClass;

import android.content.Context;

import com.sudoajay.pdf_viewer.Database_Classes.Database;

import java.io.File;
import java.util.ArrayList;

public class ScanPdf {
    private ArrayList<String> pdfPath = new ArrayList<>();
    private Context mContext;
    private Database database;

    public void scanFIle(final Context mContext, final String external_dir, final String sd_Card_dir) {

        this.mContext = mContext;
        database = new Database(mContext);

        if (new File(external_dir).exists()) {
            GetAllPath(new File(external_dir));
        }
        if (new File(sd_Card_dir).exists()) {
            GetAllPath(new File(sd_Card_dir));
        }

// Empty If the database have something
        if (!database.isEmpty())
            database.deleteData();

    }

    private void GetAllPath(final File directory) {
        String extension = ".pdf", getName;
        try {

            for (File child : directory.listFiles())
                if (child.isDirectory()) {
                    GetAllPath(child);
                } else {
                    getName = child.getName();
                    if (getName.endsWith(extension))
                        pdfPath.add(child.getAbsolutePath());
                }
        } catch (Exception ignored) {

        }
    }


    public ArrayList<String> getPdfPath() {
        return pdfPath;
    }
}
