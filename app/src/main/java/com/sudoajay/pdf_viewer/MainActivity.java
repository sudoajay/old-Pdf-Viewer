package com.sudoajay.pdf_viewer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sudoajay.pdf_viewer.Custom_Dialog.Dialog_InformationData;
import com.sudoajay.pdf_viewer.HelperClass.CopyFile;
import com.sudoajay.pdf_viewer.HelperClass.CustomToast;
import com.sudoajay.pdf_viewer.HelperClass.ScanPdf;
import com.sudoajay.pdf_viewer.Permission.AndroidExternalStoragePermission;
import com.sudoajay.pdf_viewer.Permission.AndroidSdCardPermission;
import com.sudoajay.pdf_viewer.RecyclerView.MyAdapter;
import com.sudoajay.pdf_viewer.RecyclerView.RecyclerItemClickListener;
import com.sudoajay.pdf_viewer.SdCard.SdCardPath;
import com.sudoajay.pdf_viewer.WebView.ShowWebView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private static final String TAG = "GotSomething";
    private AndroidExternalStoragePermission androidExternalStoragePermission;
    private AndroidSdCardPermission androidSdCardPermission;
    private ImageView refresh_imageView;
    private SwipeRefreshLayout swipeToRefresh;
    private ArrayList<String> getPdfPath = new ArrayList<>();
    private RecyclerView recyclerView;
    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;
    private BottomSheetDialog mBottomSheetDialog;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Reference();

//        Take Permission
        if (!androidExternalStoragePermission.isExternalStorageWritable()) {
            androidExternalStoragePermission.call_Thread();
        } else {
            new MultiThreadingScanning().execute();
        }


//        Recycler set On touch
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // do whatever

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        // do whatever
                        path = getPdfPath.get(position);
                        mBottomSheetDialog.show();
                    }
                })
        );

    }

    private void Reference() {
        refresh_imageView = findViewById(R.id.refresh_imageView);
        recyclerView = findViewById(R.id.recyclerView);
        swipeToRefresh = findViewById(R.id.swipeToRefresh);
        swipeToRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        swipeToRefresh.setOnRefreshListener(this);

        // create object


        androidExternalStoragePermission = new AndroidExternalStoragePermission(MainActivity.this, MainActivity.this);

        androidSdCardPermission = new AndroidSdCardPermission(getApplicationContext(), MainActivity.this);


        mBottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        @SuppressLint("InflateParams") View sheetView = getLayoutInflater().inflate(R.layout.layout_dialog_moreoption, null);
        mBottomSheetDialog.setContentView(sheetView);

        LinearLayout fragment_history_bottom_sheet_openFile = sheetView.findViewById(R.id.fragment_history_bottom_sheet_openFile);
        fragment_history_bottom_sheet_openFile.setOnClickListener(this);
        LinearLayout fragment_history_bottom_sheet_viewFolder = sheetView.findViewById(R.id.fragment_history_bottom_sheet_viewFolder);
        fragment_history_bottom_sheet_viewFolder.setOnClickListener(this);
        LinearLayout fragment_history_bottom_sheet_moreInfo = sheetView.findViewById(R.id.fragment_history_bottom_sheet_moreInfo);
        fragment_history_bottom_sheet_moreInfo.setOnClickListener(this);

    }

    private void fillItem() {
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        RecyclerView.Adapter mAdapter = new MyAdapter(getPdfPath);
        recyclerView.setAdapter(mAdapter);

        recyclerView.invalidate();
    }

    public void OnClick(View view) {
        switch (view.getId()) {
            case R.id.share_imageView:
                Share();
                break;
            case R.id.refresh_imageView:
                if (refresh_imageView != null && refresh_imageView.getRotation() % 360 == 0) {
                    refresh_imageView.animate().rotationBy(360f).setDuration(1000);
                    refreshList();
                }
                break;
        }
    }

    private void Share() {
        String rating_link = "https://play.google.com/store/apps/details?id=com.sudoajay.whatsapp_media_mover";
        Intent i = new Intent(android.content.Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(android.content.Intent.EXTRA_SUBJECT, "Link-Share");
        i.putExtra(android.content.Intent.EXTRA_TEXT, R.string.share_info + rating_link);
        startActivity(Intent.createChooser(i, "Share via"));
    }

    private void refreshList() {
        getPdfPath.clear();
        fillItem();

        new MultiThreadingScanning().execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {// If request is cancelled, the result arrays are empty.
            if (!(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                CustomToast.ToastIt(MainActivity.this, "Permission denied to read your External storage");

            } else {
                new MultiThreadingScanning().execute();
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // local variable
        super.onActivityResult(requestCode, resultCode, data);
        Uri sd_Card_URL;
        String sd_Card_Path_URL, string_URI;

        if (resultCode != Activity.RESULT_OK)
            return;
        sd_Card_URL = data.getData();
        MainActivity.this.grantUriPermission(MainActivity.this.getPackageName(), sd_Card_URL, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        assert sd_Card_URL != null;
        MainActivity.this.getContentResolver().takePersistableUriPermission(sd_Card_URL, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        sd_Card_Path_URL = SdCardPath.getFullPathFromTreeUri(sd_Card_URL, MainActivity.this);

        string_URI = sd_Card_URL.toString();
        assert sd_Card_Path_URL != null;
        sd_Card_Path_URL = Spilit_The_Path(string_URI, sd_Card_Path_URL);

        if (!isSelectSdRootDirectory(sd_Card_URL.toString()) || isSamePath(sd_Card_Path_URL)) {
            CustomToast.ToastIt(getApplicationContext(), getResources().getString(R.string.errorMes));
            return;
        }
        androidSdCardPermission.setSd_Card_Path_URL(sd_Card_Path_URL);
        androidSdCardPermission.setString_URI(string_URI);


    }

    private boolean isSamePath(String sd_Card_Path_URL) {
        return androidExternalStoragePermission.getExternal_Path().equals(sd_Card_Path_URL);
    }

    private boolean isSelectSdRootDirectory(String path) {
        return path.substring(path.length() - 3).equals("%3A");

    }


    private String Spilit_The_Path(final String url, final String path) {
        String[] spilt = url.split("%3A");
        String[] getPaths = spilt[0].split("/");
        String[] paths = path.split(getPaths[getPaths.length - 1]);
        return paths[0] + getPaths[getPaths.length - 1];
    }


    @Override
    public void onRefresh() {
        swipeToRefresh.setRefreshing(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                refreshList();
                swipeToRefresh.setRefreshing(false);
            }
        }, 2000);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.fragment_history_bottom_sheet_openFile:
                new MultiThreadingCopying().execute();
                break;
            case R.id.fragment_history_bottom_sheet_viewFolder:
                SpecificFolder();
                break;
            case R.id.fragment_history_bottom_sheet_moreInfo:
                Dialog_InformationData();
                mBottomSheetDialog.dismiss();
                break;

        }
    }


    private void SpecificFolder() {
        String getPath = path.replace("/" + new File(path).getName(), "");
        Uri selectedUri = Uri.parse(getPath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, "resource/folder");

        if (intent.resolveActivityInfo(getPackageManager(), 0) != null) {
            startActivity(intent);
        } else {
            CustomToast.ToastIt(getApplicationContext(), "No file explorer found");
        }
    }

    public void Dialog_InformationData() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Dialog_InformationData information_data = new Dialog_InformationData(path, MainActivity.this);
        information_data.show(ft, "dialog");
    }

    @SuppressLint("StaticFieldLeak")
    public class MultiThreadingScanning extends AsyncTask<String, String, String> {
        private ScanPdf scanPdf;

        @Override
        protected void onPreExecute() {
            getPdfPath.clear();
            scanPdf = new ScanPdf();
        }

        @Override
        protected String doInBackground(String... strings) {
            scanPdf.Duplication(MainActivity.this, androidExternalStoragePermission.getExternal_Path(), androidSdCardPermission.getSd_Card_Path_URL());

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            getPdfPath = scanPdf.getPdfPath();

            fillItem();

        }

    }

    @Override
    public void onStart() {
        super.onStart();

        swipeToRefresh.getViewTreeObserver().addOnScrollChangedListener(mOnScrollChangedListener =
                new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        if (recyclerView.getScrollY() == 0)
                            swipeToRefresh.setEnabled(true);
                        else
                            swipeToRefresh.setEnabled(false);

                    }
                });
    }

    @Override
    public void onStop() {
        swipeToRefresh.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
        super.onStop();
    }

    @SuppressLint("StaticFieldLeak")
    public class MultiThreadingCopying extends AsyncTask<String, String, String> {
        File dst;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                File src = new File(path);
                dst = new File(getExternalCacheDir() + "/" + src.getName());

//                If file exist with same size
                if (!(dst.exists() && dst.length() == src.length()))
                    CopyFile.copy(src, dst);
            } catch (Exception ignored) {

            }
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Intent intent = new Intent(getApplicationContext(), ShowWebView.class);
            intent.setAction(dst.getAbsolutePath());
            startActivity(intent);

        }
    }
}
