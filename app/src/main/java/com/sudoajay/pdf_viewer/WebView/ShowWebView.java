package com.sudoajay.pdf_viewer.WebView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.sudoajay.pdf_viewer.R;

public class ShowWebView extends AppCompatActivity {

    private WebView myWebView;
    private String getPath;
    private static final String TAG = "GotSomething";

    @SuppressLint({"WrongConstant", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_web_view);

        Intent intent = getIntent();
        if (intent != null) {
            getPath = intent.getAction();
            Log.e(TAG, "" + getPath);
        }

        setupWebView();

//        Load Url
        myWebView.loadUrl("file:///android_asset/web/viewer.html?file=" + getPath);


    }

    @SuppressLint({"WrongConstant", "SetJavaScriptEnabled", "ClickableViewAccessibility"})
    private void setupWebView() {
        myWebView = findViewById(R.id.webview);

        myWebView.setPadding(0, 0, 0, 0);
        myWebView.setInitialScale(1);
        myWebView.setScrollBarStyle(33554432);
        myWebView.setScrollbarFadingEnabled(false);
        WebSettings settings = myWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        myWebView.setWebViewClient(new WebViewClient());


    }
}
