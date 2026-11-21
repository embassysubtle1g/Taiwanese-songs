package com.embassysubtle1g.taiwanesesongs

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
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
import androidx.compose.runtime.mutableLongStateOf
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
        // 解決上方黑邊：強制設定淺色狀態列與導覽列（深色圖示 + 透明底，由外層 Box 決定底色）
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

            // 攔截返回鍵：依序處理退出全螢幕 -> 網頁回上一頁 -> 連按兩次退出 App
            BackHandler(enabled = true) {
                when {
                    customView != null -> {
                        customViewCallback?.onCustomViewHidden()
                    }
                    webView?.canGoBack() == true -> {
                        webView?.goBack()
                    }
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

                                // 移除 "; wv" 標記，避免 Google 登入跳出 403 disallowed_useragent
                                userAgentString = userAgentString.replace("; wv", "")
                            }

                            // 開啟 Cookie 與第三方 Cookie，讓 YouTube 與 Google 帳號登入狀態互通並保存
                            val currentWebView = this
                            CookieManager.getInstance().apply {
                                setAcceptCookie(true)
                                setAcceptThirdPartyCookies(currentWebView, true)
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
                                    val scheme = uri.scheme?.lowercase() ?: return false
                                    val host = uri.host?.lowercase() ?: ""

                                    // 1. 阻擋 Google 登入完成後嘗試透過 intent:// 喚起外部 YouTube App 或瀏覽器
                                    if (scheme == "intent") {
                                        // 如果 intent 裡有夾帶 fallback 網址，抓出來留在 App 內載入
                                        val fallbackUrl = uri.getQueryParameter("browser_fallback_url")
                                        if (!fallbackUrl.isNullOrEmpty()) {
                                            view.loadUrl(fallbackUrl)
                                        }
                                        return true // 直接攔截，不跳出 App
                                    }

                                    // 2. 放行你的網站，以及所有 Google / YouTube 相關網域（包含各國網域如 .com.tw、g.co 等）
                                    val isAppWebsite =
                                        scheme == "https" && host == "embassysubtle1g.github.io"

                                    val isGoogleOrYouTube =
                                        scheme == "https" && (
                                                host == "g.co" ||
                                                        host == "youtu.be" ||
                                                        host.endsWith(".youtu.be") ||
                                                        host.contains("google.") ||        // 涵蓋 accounts.google.com、google.com.tw 等所有國別網域
                                                        host.contains("youtube.") ||       // 涵蓋 m.youtube.com、youtube-nocookie.com 等
                                                        host.endsWith(".gstatic.com") ||
                                                        host.endsWith(".googleapis.com") ||
                                                        host.endsWith(".googleusercontent.com")
                                                )

                                    if (isAppWebsite || isGoogleOrYouTube) {
                                        return false // 全部留在你的 App 內載入
                                    }

                                    // 3. 其他真正屬於第三方的外部網站才交給外部瀏覽器
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