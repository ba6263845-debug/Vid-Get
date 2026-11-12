package com.example.presentation.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.domain.model.MediaFormatOption
import com.example.domain.model.MediaItem
import com.example.presentation.AppViewModel
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TealPrimary
import java.io.ByteArrayInputStream

data class SniffedMedia(
    val title: String,
    val url: String,
    val mimeType: String,
    val sizeBytes: Long = 25 * 1024 * 1024L
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    viewModel: AppViewModel,
    initialUrl: String? = null,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    var currentUrl by remember { mutableStateOf(initialUrl ?: "https://archive.org/details/movies") }
    var inputUrl by remember { mutableStateOf(currentUrl) }
    var pageTitle by remember { mutableStateOf("Browser") }
    var progress by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    var isIncognito by remember { mutableStateOf(false) }
    var isAdBlockEnabled by remember { mutableStateOf(true) }
    var isBookmarked by remember { mutableStateOf(false) }
    var hasRendererCrashed by remember { mutableStateOf(false) }

    val detectedMediaList = remember { mutableStateListOf<SniffedMedia>() }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(initialUrl) {
        if (!initialUrl.isNullOrBlank() && initialUrl != currentUrl) {
            currentUrl = initialUrl
            inputUrl = initialUrl
            hasRendererCrashed = false
            webViewRef?.loadUrl(initialUrl)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("browser_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Address & Tool Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackToHome) {
                            Icon(Icons.Default.Home, contentDescription = "Home", tint = TealPrimary)
                        }

                        OutlinedTextField(
                            value = inputUrl,
                            onValueChange = { inputUrl = it },
                            placeholder = { Text("Search or type URL...", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            trailingIcon = {
                                if (inputUrl.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            var target = inputUrl.trim()
                                            if (!target.startsWith("http://") && !target.startsWith("https://")) {
                                                target = if (target.contains(".") && !target.contains(" ")) {
                                                    "https://$target"
                                                } else {
                                                    "https://duckduckgo.com/?q=${target.replace(" ", "+")}"
                                                }
                                            }
                                            currentUrl = target
                                            inputUrl = target
                                            hasRendererCrashed = false
                                            webViewRef?.loadUrl(target)
                                        }
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Go", tint = TealPrimary)
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = TealPrimary,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("browser_url_input")
                        )

                        // Adblocker toggle
                        IconButton(onClick = { isAdBlockEnabled = !isAdBlockEnabled }) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Adblocker",
                                tint = if (isAdBlockEnabled) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Incognito toggle
                        IconButton(onClick = { isIncognito = !isIncognito }) {
                            Icon(
                                imageVector = if (isIncognito) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Incognito",
                                tint = if (isIncognito) AccentOrange else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Bottom mini navigation row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            IconButton(
                                onClick = { if (webViewRef?.canGoBack() == true) webViewRef?.goBack() }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp))
                            }
                            IconButton(
                                onClick = { if (webViewRef?.canGoForward() == true) webViewRef?.goForward() }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward", modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = {
                                hasRendererCrashed = false
                                webViewRef?.reload()
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reload", modifier = Modifier.size(20.dp))
                            }
                        }

                        Text(
                            text = if (isIncognito) "🕶️ Incognito" else pageTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false).padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = {
                                isBookmarked = !isBookmarked
                            }
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) AccentOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Progress Bar
            if (isLoading && progress < 100) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    color = AccentOrange,
                    trackColor = Color.Transparent,
                    modifier = Modifier.fillMaxWidth().height(2.dp)
                )
            }

            if (hasRendererCrashed) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = AccentOrange,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Web Session Recovered",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "The browser sandbox was refreshed to save memory.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                hasRendererCrashed = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reload Web Page")
                        }
                    }
                }
            } else {
                // Main WebView with software layer to prevent GPU rendernode crashes
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                cacheMode = if (isIncognito) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
                                userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36 VidGet/1.0"
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    progress = newProgress
                                    isLoading = newProgress < 100
                                }

                                override fun onReceivedTitle(view: WebView?, title: String?) {
                                    if (!title.isNullOrBlank()) {
                                        pageTitle = title
                                    }
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    isLoading = true
                                    url?.let {
                                        inputUrl = it
                                        currentUrl = it
                                    }
                                    detectedMediaList.clear()
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoading = false
                                }

                                override fun onRenderProcessGone(
                                    view: WebView?,
                                    detail: RenderProcessGoneDetail?
                                ): Boolean {
                                    // Crucial: return true so Android does not terminate the app!
                                    view?.let {
                                        (it.parent as? ViewGroup)?.removeView(it)
                                        it.destroy()
                                    }
                                    webViewRef = null
                                    hasRendererCrashed = true
                                    return true
                                }

                                override fun shouldInterceptRequest(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): WebResourceResponse? {
                                    val reqUrl = request?.url?.toString() ?: return null

                                    // Simple AdBlocker filter
                                    if (isAdBlockEnabled) {
                                        val adPatterns = listOf("doubleclick.net", "googlesyndication.com", "adnxs.com", "/ads.js", "popads.net")
                                        if (adPatterns.any { reqUrl.contains(it) }) {
                                            return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))
                                        }
                                    }

                                    // Media Sniffer for video and audio formats
                                    if (reqUrl.endsWith(".mp4") || reqUrl.contains(".mp4?") ||
                                        reqUrl.endsWith(".mp3") || reqUrl.contains(".mp3?") ||
                                        reqUrl.endsWith(".webm") || reqUrl.contains(".webm?") ||
                                        reqUrl.contains(".m3u8")
                                    ) {
                                        val isAudio = reqUrl.contains(".mp3")
                                        val name = reqUrl.substringAfterLast("/").substringBefore("?").ifBlank { "Detected_Media" }
                                        if (detectedMediaList.none { it.url == reqUrl }) {
                                            post {
                                                detectedMediaList.add(
                                                    SniffedMedia(
                                                        title = name,
                                                        url = reqUrl,
                                                        mimeType = if (isAudio) "audio/mpeg" else "video/mp4"
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    return super.shouldInterceptRequest(view, request)
                                }
                            }

                            webViewRef = this
                            loadUrl(currentUrl)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("in_app_webview")
                )
            }
        }

        // VidMate Floating Media Download Button with Badge!
        AnimatedVisibility(
            visible = detectedMediaList.isNotEmpty(),
            enter = scaleIn(),
            exit = scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 100.dp)
        ) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = TealPrimary,
                        contentColor = DeepNavy
                    ) {
                        Text(
                            text = "${detectedMediaList.size}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            ) {
                FloatingActionButton(
                    onClick = {
                        val firstMedia = detectedMediaList.firstOrNull()
                        if (firstMedia != null) {
                            val mediaItem = MediaItem(
                                id = "sniffed_${System.currentTimeMillis()}",
                                title = firstMedia.title,
                                author = pageTitle,
                                thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&q=80",
                                duration = "Online Media",
                                views = "Web Stream",
                                streamUrl = firstMedia.url,
                                license = "Direct Downloadable Web Media",
                                isDownloadAllowed = true,
                                category = "Downloads",
                                availableQualities = listOf(
                                    MediaFormatOption("Best Quality (MP4)", "mp4", 35 * 1024 * 1024L, false, firstMedia.url),
                                    MediaFormatOption("Audio Extract (MP3)", "mp3", 6 * 1024 * 1024L, true, firstMedia.url)
                                )
                            )
                            viewModel.openDownloadSheet(mediaItem)
                        }
                    },
                    containerColor = AccentOrange,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("browser_floating_download_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download detected media",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
