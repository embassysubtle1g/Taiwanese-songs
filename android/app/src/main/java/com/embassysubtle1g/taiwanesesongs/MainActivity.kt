package com.embassysubtle1g.taiwanesesongs

// 在 import 區塊補上這兩個
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.runtime.mutableLongStateOf

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.viewinterop.AndroidView

private const val WEBSITE_URL =
    "https://embassysubtle1g.github.io/Taiwanese-songs/?app=android"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. 解決上方黑邊：強制設定淺色狀態列與導覽列（深色圖示 + 透明底，由外層 Box 決定底色）
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        WebView.setWebContentsDebuggingEnabled(true)

        setContent {
            var webView by remember { mutableStateOf<WebView?>(null) }
            // 用 Compose State 即時追蹤 WebView 是否能返回上一頁
            var canGoBack by remember { mutableStateOf(false) }
            var lastBackPressTime by remember { mutableLongStateOf(0L) }

            var customView by remember { mutableStateOf<View?>(null) }
            var customViewCallback by remember {
                mutableStateOf<WebChromeClient.CustomViewCallback?>(null)
            }

            // 無條件攔截返回鍵（enabled = true），自行決定要退全螢幕、回上一頁或提示退出
            BackHandler(enabled = true) {
                when {
                    // 1. 若正在全螢幕看影片，先退出全螢幕
                    customView != null -> {
                        customViewCallback?.onCustomViewHidden()
                    }
                    // 2. 若 WebView 有上一頁（包含 YouTube 頁面退回你的歌單網頁），返回上一頁
                    webView?.canGoBack() == true -> {
                        webView?.goBack()
                    }
                    // 3. 已經在首頁沒有上一頁了，連按兩次才退出 App
                    else -> {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastBackPressTime < 2000L) {
                            finish()
                        } else {
                            lastBackPressTime = currentTime
                            Toast.makeText(
                                this@MainActivity,
                                "再按一次返回鍵退出應用程式",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ComposeColor.White)
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars),
                    factory = { context ->
                        WebView(context).apply {
                            webView = this

                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                loadsImagesAutomatically = true
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                mediaPlaybackRequiresUserGesture = false
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onShowCustomView(
                                    view: View?,
                                    callback: CustomViewCallback?
                                ) {
                                    if (customView != null) {
                                        callback?.onCustomViewHidden()
                                        return
                                    }
                                    customView = view
                                    customViewCallback = callback
                                }

                                override fun onHideCustomView() {
                                    customView = null
                                    customViewCallback?.onCustomViewHidden()
                                    customViewCallback = null
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                // 每次頁面開始載入、完成載入或前端路由切換時，更新 canGoBack 狀態
                                override fun onPageStarted(
                                    view: WebView?,
                                    url: String?,
                                    favicon: Bitmap?
                                ) {
                                    canGoBack = view?.canGoBack() == true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    canGoBack = view?.canGoBack() == true
                                }

                                override fun doUpdateVisitedHistory(
                                    view: WebView?,
                                    url: String?,
                                    isReload: Boolean
                                ) {
                                    canGoBack = view?.canGoBack() == true
                                }

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

                customView?.let { view ->
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ComposeColor.Black),
                        factory = { context ->
                            FrameLayout(context).apply {
                                addView(
                                    view,
                                    FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}