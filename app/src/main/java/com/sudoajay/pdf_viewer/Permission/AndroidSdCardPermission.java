package com.sudoajay.pdf_viewer.Permission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.sudoajay.pdf_viewer.MainActivity;
import com.sudoajay.pdf_viewer.SdCard.SdCardDialog;
import com.sudoajay.pdf_viewer.SharedPreference.SdCardPathSharedPreference;

import java.io.File;

@SuppressLint("Registered")
public class AndroidSdCardPermission {
    private Context context;
    private String sd_Card_Path_URL = "", string_URI;


    private SdCardPathSharedPreference sdCardPathSharedPreference;
    private Activity activity;

    public AndroidSdCardPermission(final Context context ,final Activity activity) {
        this.context = context;
        this.activity = activity;
        Grab();
    }

    public AndroidSdCardPermission(Context context) {
        this.context = context;
        Grab();
    }


    public void call_Thread() {
        if (!isSdStorageWritable()) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    Storage_Access_FrameWork();
                }
            }, 1800);
        }
    }

    public void Storage_Access_FrameWork() {
        try {
            final Intent intent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

                int REQUEST_CODE_OPEN_DOCUMENT_TREE = 42;


                activity.startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);

            }
        } catch (Exception e) {
            Toast.makeText(context, "There is Error Please Report It",Toast.LENGTH_LONG).show();
        }
    }

    private void Call_Custom_Dailog_Changes() {
        try {
            FragmentTransaction ft = (((FragmentActivity) activity)).getSupportFragmentManager().beginTransaction();
            SdCardDialog sd_card_dialog = new SdCardDialog(this);
            sd_card_dialog.show(ft, "dialog");
        } catch (Exception ignored) {

        }
    }

    public boolean isSdStorageWritable() {
        return (!sd_Card_Path_URL.equals(Environment.getExternalStorageDirectory().getAbsolutePath()) &&
                new File(sd_Card_Path_URL).exists() && new File(sd_Card_Path_URL).listFiles() != null);
    }

    private void Grab() {
        // gran the data from shared preference
        sdCardPathSharedPreference = new SdCardPathSharedPreference(context);
        try {

            sd_Card_Path_URL = sdCardPathSharedPreference.getSdCardPath();
            string_URI = sdCardPathSharedPreference.getStringURI();
        } catch (Exception ignored) {


        }
    }

    public String getSd_Card_Path_URL() {
        return sd_Card_Path_URL;
    }


    public String getString_URI() {
        return string_URI;
    }

    public void setSd_Card_Path_URL(String sd_Card_Path_URL) {
        this.sd_Card_Path_URL = sd_Card_Path_URL;
        sdCardPathSharedPreference.setSdCardPath(sd_Card_Path_URL);
    }

    public void setString_URI(String string_URI) {
        this.string_URI = string_URI;
        sdCardPathSharedPreference.setStringURI(string_URI);

    }
}
