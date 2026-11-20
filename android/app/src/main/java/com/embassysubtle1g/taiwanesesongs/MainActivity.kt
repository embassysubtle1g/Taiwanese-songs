package com.embassysubtle1g.taiwanesesongs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val API_URL =
    "https://script.google.com/macros/s/AKfycbxoFMPM8tuVUImuzEGGPC7_goxO_OBhTCe3oUSNcw3zP7sav9-X5xygVx0DjyycGRi_/exec"

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String
)

data class Song(
    val id: String,
    val artistId: String,
    val name: String,
    val youtubeUrl: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TaiwaneseSongsApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaiwaneseSongsApp() {
    var artists by remember { mutableStateOf<List<Artist>>(emptyList()) }
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var selectedArtist by remember { mutableStateOf<Artist?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }
    val context = LocalContext.current

    BackHandler(enabled = selectedArtist != null) {
        selectedArtist = null
    }

    LaunchedEffect(reloadKey) {
        loading = true
        errorMessage = null

        try {
            val result = loadMusicData(context)
            artists = result.first
            songs = result.second
        } catch (error: Exception) {
            errorMessage = error.message ?: "目前無法載入資料，請稍後再試。"
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedArtist?.name ?: "歌手",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (selectedArtist != null) {
                        IconButton(onClick = { selectedArtist = null }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回歌手列表"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                loading -> LoadingView()
                errorMessage != null -> ErrorView(
                    message = errorMessage ?: "載入失敗",
                    onRetry = { reloadKey++ }
                )
                selectedArtist == null -> ArtistList(
                    artists = artists,
                    onArtistClick = { selectedArtist = it }
                )
                else -> SongList(
                    songs = songs.filter { it.artistId == selectedArtist?.id },
                    onSongClick = { song ->
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(song.youtubeUrl)
                        )
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(12.dp))
            Text("載入中...")
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onRetry) {
                Text("重新載入")
            }
        }
    }
}

@Composable
private fun ArtistList(
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(artists, key = { it.id }) { artist ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onArtistClick(artist) },
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = artist.imageUrl.ifBlank { null },
                        contentDescription = artist.name,
                        modifier = Modifier.size(76.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        text = artist.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SongList(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 8.dp
        )
    ) {
        items(songs, key = { it.id }) { song ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSongClick(song) }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = song.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            HorizontalDivider()
        }

        if (songs.isEmpty()) {
            item {
                Text(
                    text = "目前沒有歌曲資料",
                    modifier = Modifier.padding(vertical = 24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 使用 WebView 執行 JSONP。
 *
 * 網站已經透過 JSONP 正常讀取 Apps Script；Android 也沿用相同方式，
 * 避免原生 HTTP 用戶端在 Apps Script 跨網域重新導向時遇到 404。
 */
private suspend fun loadMusicData(
    context: android.content.Context
): Pair<List<Artist>, List<Song>> {
    val artistsJson = fetchJsonpResponse(context, "getArtists")
    val artists = parseApiArray(artistsJson).map { item ->
        Artist(
            id = item.optString("ArtistID"),
            name = item.optString("ArtistName"),
            imageUrl = item.optString("ImageURL")
        )
    }

    val songsJson = fetchJsonpResponse(context, "getSongs")
    val songs = parseApiArray(songsJson).map { item ->
        Song(
            id = item.optString("SongID"),
            artistId = item.optString("ArtistID"),
            name = item.optString("SongName"),
            youtubeUrl = item.optString("YouTubeURL")
        )
    }

    return Pair(artists, songs)
}

private fun parseApiArray(responseText: String): List<JSONObject> {
    val response = JSONObject(responseText)

    if (!response.optBoolean("success")) {
        throw IllegalStateException(
            response.optString("error", "API 回傳錯誤。")
        )
    }

    val data = response.optJSONArray("data") ?: JSONArray()
    return (0 until data.length()).mapNotNull { index ->
        data.optJSONObject(index)
    }
}

/**
 * 建立短暫的 WebView，透過 JSONP 取得 Apps Script 回應。
 * WebView 不會顯示在畫面上，只負責完成資料請求。
 */
private suspend fun fetchJsonpResponse(
    context: android.content.Context,
    action: String
): String = withTimeout(30_000L) {
    withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            val webView = WebView(context.applicationContext)
            webView.settings.javaScriptEnabled = true
            
            fun finishWithResult(result: String?, error: Throwable?) {
                webView.post {
                    if (continuation.isActive) {
                        if (error != null) {
                            continuation.resumeWithException(error)
                        } else {
                            continuation.resume(result.orEmpty())
                        }
                    }
                    webView.stopLoading()
                    webView.removeJavascriptInterface("AndroidBridge")
                    webView.destroy()
                }
            }

            val bridge = object {
                @JavascriptInterface
                fun onResponse(response: String) {
                    finishWithResult(response, null)
                }

                @JavascriptInterface
                fun onError(message: String) {
                    finishWithResult(
                        null,
                        IllegalStateException("JSONP 讀取 $action 失敗：$message")
                    )
                }
            }

            webView.addJavascriptInterface(bridge, "AndroidBridge")
            webView.webViewClient = WebViewClient()

            continuation.invokeOnCancellation {
                webView.post {
                    webView.stopLoading()
                    webView.removeJavascriptInterface("AndroidBridge")
                    webView.destroy()
                }
            }

            val html = """
                <!doctype html>
                <html>
                  <head><meta charset="utf-8"></head>
                  <body>
                    <script>
                      window.taiwaneseSongsCallback = function(response) {
                        AndroidBridge.onResponse(JSON.stringify(response));
                      };
                      const script = document.createElement('script');
                      script.src = '$API_URL?action=$action&callback=taiwaneseSongsCallback';
                      script.onerror = function() {
                        AndroidBridge.onError('無法載入 Apps Script 回應');
                      };
                      document.head.appendChild(script);
                    </script>
                  </body>
                </html>
            """.trimIndent()

            webView.loadDataWithBaseURL(
                "https://script.google.com/",
                html,
                "text/html",
                "UTF-8",
                null
            )
        }
    }
}
