package com.embassysubtle1g.taiwanesesongs

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

private const val WEBSITE_URL =
    "https://embassysubtle1g.github.io/Taiwanese-songs/?app=android"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 15（targetSdk 35+）採用 Edge-to-Edge；
        // 淺色 App 背景搭配深色系統列圖示，避免狀態列資訊看不見。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        }

        setContent {
            var webView by remember { mutableStateOf<WebView?>(null) }

            BackHandler(enabled = webView?.canGoBack() == true) {
                webView?.goBack()
            }

            AndroidView(
                // 將網頁內容限制在系統狀態列與導覽列之間，
                // 避免 Header 被狀態列覆蓋。
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars),
                factory = { context ->
                    WebView(context).apply {
                        webView = this

                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadsImagesAutomatically = true

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView,
                                request: WebResourceRequest
                            ): Boolean {
                                val uri = request.url
                                val host = uri.host?.lowercase()
                                val isAppWebsite =
                                    uri.scheme == "https" &&
                                    host == "embassysubtle1g.github.io"
                                val isYouTube =
                                    uri.scheme == "https" &&
                                    host in setOf(
                                        "youtube.com",
                                        "www.youtube.com",
                                        "m.youtube.com",
                                        "youtu.be",
                                        "www.youtu.be",
                                        "youtube-nocookie.com",
                                        "www.youtube-nocookie.com"
                                    )

                                if (isAppWebsite || isYouTube) {
                                    return false
                                }

                                return try {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, uri)
                                    )
                                    true
                                } catch (_: Exception) {
                                    true
                                }
                            }
                        }

                        loadUrl(WEBSITE_URL)
                    }
                },
                update = { view ->
                    if (webView !== view) {
                        webView = view
                    }
                }
            )
        }
    }
}
