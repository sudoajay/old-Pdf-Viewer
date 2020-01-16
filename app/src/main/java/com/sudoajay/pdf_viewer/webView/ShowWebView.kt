package com.sudoajay.pdf_viewer.webView

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.sudoajay.pdf_viewer.R


class ShowWebView : AppCompatActivity() {
    private var myWebView: WebView? = null
    private var getPath: String? = null
    @SuppressLint("WrongConstant", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_web_view)
        val intent = intent
        if (intent != null) {
            getPath = intent.action
        }
        setupWebView()
        //        Load Url
        myWebView!!.loadUrl("file:///android_asset/web/viewer.html?file=$getPath")
    }

    @SuppressLint("WrongConstant", "SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun setupWebView() {
        myWebView = findViewById(R.id.webview)
        myWebView?.setPadding(0, 0, 0, 0)
        myWebView?.setInitialScale(1)
        myWebView?.scrollBarStyle = 33554432
        myWebView?.isScrollbarFadingEnabled = false
        val settings: WebSettings? = myWebView?.settings
        settings?.javaScriptEnabled = true
        settings?.allowFileAccessFromFileURLs = true
        settings?.allowUniversalAccessFromFileURLs = true
        settings?.builtInZoomControls = true
        settings?.setSupportZoom(true)
        settings?.useWideViewPort = true
        settings?.loadWithOverviewMode = true
        myWebView?.webViewClient = WebViewClient()
    }
}