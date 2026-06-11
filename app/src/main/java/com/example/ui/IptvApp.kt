package com.example.ui

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun IptvApp() {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    // Intercept Back Press to navigate within WebView history
    BackHandler(enabled = webViewRef?.canGoBack() == true || customView != null) {
        if (customView != null) {
            // Exit fullscreen if active
            customViewCallback?.onCustomViewHidden()
        } else {
            webViewRef?.goBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // Match deepBlueBg
    ) {
        if (customView != null) {
            // Fullscreen video playback overlay view
            AndroidView(
                factory = {
                    FrameLayout(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        addView(customView)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Standard WebView Layout with top/bottom safe windows padding
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewRef = this
                        
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // Optimize hardware acceleration and performance parameters
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            allowFileAccess = true
                            allowContentAccess = true
                            mediaPlaybackRequiresUserGesture = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                        }

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                url: String?
                            ): Boolean {
                                return false // Handle everything inside WebView
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            // Support Web Fullscreen (YouTube style)
                            override fun onShowCustomView(
                                view: View?,
                                callback: CustomViewCallback?
                            ) {
                                super.onShowCustomView(view, callback)
                                customView = view
                                customViewCallback = callback
                            }

                            override fun onHideCustomView() {
                                super.onHideCustomView()
                                customView = null
                                customViewCallback = null
                            }
                        }

                        loadUrl("file:///android_asset/index.html")
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                update = {
                    // Retain state through compositions
                }
            )
        }
    }
}
