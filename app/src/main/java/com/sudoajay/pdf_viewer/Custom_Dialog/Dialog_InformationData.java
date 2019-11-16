package com.sudoajay.pdf_viewer.Custom_Dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.sudoajay.pdf_viewer.HelperClass.CustomToast;
import com.sudoajay.pdf_viewer.HelperClass.FileSize;
import com.sudoajay.pdf_viewer.MainActivity;
import com.sudoajay.pdf_viewer.R;

import java.io.File;
import java.text.SimpleDateFormat;

public class Dialog_InformationData extends DialogFragment implements View.OnClickListener {

    private String path;
    private View rootview;
    private ConstraintLayout constraintLayout;
    private TextView infoName_TextView, infoLocation_TextView, infoSize_TextView, infoType_TextView, infoExt_TextView, infoCreated_TextView;
    private Activity activity;
    private MainActivity mainActivity;

    public Dialog_InformationData(final String path, final MainActivity mainActivity) {
        this.path = path;
        this.mainActivity = mainActivity;
        this.activity = mainActivity;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootview = inflater.inflate(R.layout.layout_dialog_informationdata, container, false);
        Reference();
        // setup dialog box
        (getDialog()).getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        constraintLayout.setBackgroundColor(getResources().getColor(R.color.tabBackgroundColor));
        // Fill Dialog Box
        FillIt();

        return rootview;
    }

    private void Reference() {
        // Reference Object
        constraintLayout = rootview.findViewById(R.id.constraintLayout);
        infoName_TextView = rootview.findViewById(R.id.infoName_TextView);
        infoLocation_TextView = rootview.findViewById(R.id.infoLocation_TextView);
        infoSize_TextView = rootview.findViewById(R.id.infoSize_TextView);
        infoType_TextView = rootview.findViewById(R.id.infoType_TextView);
        infoExt_TextView = rootview.findViewById(R.id.infoExt_TextView);
        infoCreated_TextView = rootview.findViewById(R.id.infoCreated_TextView);

        ImageView close_ImageView = rootview.findViewById(R.id.close_ImageView);
        close_ImageView.setOnClickListener(this);

        Button cancel_Button = rootview.findViewById(R.id.cancel_Button);
        cancel_Button.setOnClickListener(this);

        Button open_Button = rootview.findViewById(R.id.open_Button);
        open_Button.setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    private void FillIt() {
        File filePath = new File(path);
        infoName_TextView.setText(filePath.getName());

        String extactPath = path.replace(filePath.getName(), "");
        infoLocation_TextView.setText(extactPath);

        infoSize_TextView.setText(FileSize.Convert_It(filePath.length()));

        if (filePath.isDirectory()) {
            infoType_TextView.setText("Folder");
        } else {
            infoType_TextView.setText(getMimeType(path));
        }


        String fileName = filePath.getName();
        int i = fileName.lastIndexOf('.');
        int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

        if (i > p) {
            infoExt_TextView.setText(fileName.substring(i + 1));
        }


        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy , HH:mm:ss");
        infoCreated_TextView.setText(sdf.format(filePath.lastModified()));

    }

    public void onStart() {
        // This MUST be called first! Otherwise the view tweaking will not be present in the displayed Dialog (most likely overriden)
        super.onStart();

        forceWrapContent(this.getView());
    }

    private void forceWrapContent(View v) {
        // Start with the provided view
        View current = v;
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        // Travel up the tree until fail, modifying the LayoutParams
        do {
            // Get the parent
            ViewParent parent = current.getParent();

            // Check if the parent exists
            if (parent != null) {
                // Get the view
                try {
                    current = (View) parent;
                } catch (ClassCastException e) {
                    // This will happen when at the top view, it cannot be cast to a View
                    break;
                }

                // Modify the layout
                current.getLayoutParams().width = width - ((10 * width) / 100);

            }
        } while (current.getParent() != null);

        // Request a layout to be re-done
        current.requestLayout();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {

        super.onDismiss(dialog);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_Button:
                mainActivity.new MultiThreadingCopying().execute();
                dismiss();
                break;
            case R.id.cancel_Button:
            case R.id.close_ImageView:
                dismiss();
                break;
        }
    }

    private static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

}
