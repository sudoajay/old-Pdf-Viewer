package com.sudoajay.pdf_viewer.Permission;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;

import com.sudoajay.pdf_viewer.R;


public class AndroidExternalStoragePermission {

    private Context context;
    private Activity activity;
    private String external_Path;

    public AndroidExternalStoragePermission(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        external_Path = Environment.getExternalStorageDirectory().getAbsolutePath();
    }


    private void Storage_Permission_Granted() {

        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
//                if (ContextCompat.checkSelfPermission(context,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    int my_Permission_Request = 1;
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//
//                        ActivityCompat.requestPermissions(activity,
//                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, my_Permission_Request);
//                    } else {
//
//                        ActivityCompat.requestPermissions(activity,
//                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, my_Permission_Request);
//                    }
//                }

        } else {

        }
    }


    public void call_Thread() {
        // check if permission already given or not
        if (!isExternalStorageWritable()) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Call_Custom_Permission_Dailog();

                }
            }, 500);
        }
    }

    private void Call_Custom_Permission_Dailog() {
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.activity_custom_dialog_permission);
        Button button_Learn_More = dialog.findViewById(R.id.see_More_button);
        Button button_Continue = dialog.findViewById(R.id.continue_Button);
        // if button is clicked, close the custom dialog

        button_Learn_More.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final String url = "https://developer.android.com/training/permissions/requesting.html";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    activity.startActivity(i);
                } catch (Exception ignored) {

                }
            }
        });
        button_Continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Storage_Permission_Granted();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public boolean isExternalStorageWritable() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = activity.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public String getExternal_Path() {
        return external_Path;
    }
}
