package com.sudoajay.pdf_viewer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sudoajay.lodinganimation.LoadingAnimation;
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

;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private AndroidExternalStoragePermission androidExternalStoragePermission;
    private AndroidSdCardPermission androidSdCardPermission;
    private ImageView refresh_imageView;
    private SwipeRefreshLayout swipeToRefresh;
    private ArrayList<String> getPdfPath = new ArrayList<>();
    private RecyclerView recyclerView;
    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;
    private final int requestCode = 100;
    private String path;
    private BottomSheetDialog mBottomSheetDialog, bottomSheetDialog1;
    private LoadingAnimation loadingAnimation;
    private Uri fileUri;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Reference();

//        Take Permission
        if (!androidExternalStoragePermission.isExternalStorageWritable()) {
            mBottomSheetDialog.show();
        } else {
            new MultiThreadingScanning().execute();
        }


//        Recycler set On touch
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        path = getPdfPath.get(position);
                        // do whatever
                        new MultiThreadingCopying().execute();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        // do whatever
                        path = getPdfPath.get(position);
                        bottomSheetDialog1.show();
                    }
                })
        );

    }

    private void Reference() {
        refresh_imageView = findViewById(R.id.refresh_imageView);
        recyclerView = findViewById(R.id.recyclerView);
        swipeToRefresh = findViewById(R.id.swipeToRefresh);
        swipeToRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        loadingAnimation = findViewById(R.id.loadingAnimation);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeToRefresh.setOnRefreshListener(this);

        // create object


        androidExternalStoragePermission = new AndroidExternalStoragePermission(MainActivity.this, MainActivity.this);

        androidSdCardPermission = new AndroidSdCardPermission(getApplicationContext(), MainActivity.this);

//         Select Option
        mBottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        @SuppressLint("InflateParams") View sheetView = getLayoutInflater().inflate(R.layout.layout_dialog_selectoption, null);
        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.setCancelable(false);
        LinearLayout bottomSheet_scanFile = sheetView.findViewById(R.id.bottomSheet_scanFile);
        bottomSheet_scanFile.setOnClickListener(this);
        LinearLayout bottomSheet_selectFile = sheetView.findViewById(R.id.bottomSheet_selectFile);
        bottomSheet_selectFile.setOnClickListener(this);


//        More Option for file
        bottomSheetDialog1 = new BottomSheetDialog(MainActivity.this);
        @SuppressLint("InflateParams") View sheetView1 = getLayoutInflater().inflate(R.layout.layout_dialog_moreoption, null);
        bottomSheetDialog1.setContentView(sheetView1);

        LinearLayout bottomSheet_openFile = sheetView1.findViewById(R.id.bottomSheet_openFile);
        bottomSheet_openFile.setOnClickListener(this);
        LinearLayout bottomSheet_viewFolder = sheetView1.findViewById(R.id.bottomSheet_viewFolder);
        bottomSheet_viewFolder.setOnClickListener(this);
        LinearLayout bottomSheet_moreInfo = sheetView1.findViewById(R.id.bottomSheet_moreInfo);
        bottomSheet_moreInfo.setOnClickListener(this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        return true;
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

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        switch (item.getItemId()) {
//            case R.id.action_search:
//                //add the function to perform here
//                return (true);
//            case R.id.action_refresh:
//                //add the function to perform here
//                return (true);
//
//        }
//        return (super.onOptionsItemSelected(item));
//    }

    public void OnClick(View view) {
        switch (view.getId()) {
//            case R.id.search_imageView:
//                Share();
//                break;
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
        CustomToast.ToastIt(getApplicationContext(), "Refreshing");
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
                CustomToast.ToastIt(MainActivity.this, "Give us permission for further process ");

                if (!androidExternalStoragePermission.isExternalStorageWritable())
                    androidExternalStoragePermission.call_Thread();
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

        if (this.requestCode == requestCode && data != null) {
            fileUri = data.getData();
            mBottomSheetDialog.cancel();
            new MultiThreadingCopying().execute();

            return;
        }
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

    private String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
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
            case R.id.bottomSheet_scanFile:
                if (!androidExternalStoragePermission.isExternalStorageWritable())
                    androidExternalStoragePermission.call_Thread();
                mBottomSheetDialog.cancel();
                break;
            case R.id.bottomSheet_selectFile:
                openFileManager();
                break;
            case R.id.bottomSheet_openFile:
                new MultiThreadingCopying().execute();
                break;
            case R.id.bottomSheet_viewFolder:
                SpecificFolder();
                break;
            case R.id.bottomSheet_moreInfo:
                Dialog_InformationData();
                bottomSheetDialog1.dismiss();
                break;

        }
    }

    private void openFileManager() {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Set your required file type
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pdf File"), requestCode);
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
            loadingAnimation.start();
            getPdfPath.clear();
            scanPdf = new ScanPdf();
        }

        @Override
        protected String doInBackground(String... strings) {
            scanPdf.scanFIle(MainActivity.this, androidExternalStoragePermission.getExternal_Path(), androidSdCardPermission.getSd_Card_Path_URL());

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            loadingAnimation.stop();
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
                if (fileUri == null) {

                    File src = new File(path);
                    dst = new File(getCacheDir() + "/" + src.getName());

//                If file exist with same size
                    if (!dst.exists())
                        CopyFile.copy(src, dst);

                } else {
                    dst = new File(getCacheDir() + "/" + queryName(getContentResolver(), fileUri));
                    if (!dst.exists())
                        CopyFile.copyUri(MainActivity.this, fileUri, dst);
                }
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
