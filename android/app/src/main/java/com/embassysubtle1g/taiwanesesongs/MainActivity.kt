package com.embassysubtle1g.taiwanesesongs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
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

        setContent {
            var webView by remember { mutableStateOf<WebView?>(null) }

            BackHandler(enabled = webView?.canGoBack() == true) {
                webView?.goBack()
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
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
