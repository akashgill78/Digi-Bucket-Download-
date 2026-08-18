package com.example.ui.browser

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

class SnifferJsBridge(
    private val onMediaDetected: (videoUrl: String, title: String, thumbnail: String) -> Unit
) {
    @JavascriptInterface
    fun onMediaFound(videoUrl: String, title: String, thumbnail: String) {
        if (videoUrl.isNotBlank()) {
            onMediaDetected(videoUrl, title, thumbnail)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SnifferWebView(
    url: String,
    modifier: Modifier = Modifier,
    onPageStarted: (String) -> Unit = {},
    onPageFinished: (String) -> Unit = {},
    onProgressChanged: (Int) -> Unit = {},
    onMediaSniffed: (videoUrl: String, title: String, thumbnail: String) -> Unit = { _, _, _ -> },
    onWebViewCreated: (WebView) -> Unit = {}
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    loadsImagesAutomatically = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                }

                addJavascriptInterface(SnifferJsBridge(onMediaSniffed), "AndroidSniffer")

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        onProgressChanged(newProgress)
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        url?.let { onPageStarted(it) }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        url?.let { onPageFinished(it) }

                        // Inject media detector script into the active page
                        val snifferScript = """
                            (function() {
                                function checkMedia() {
                                    // Check video tags
                                    var videos = document.querySelectorAll('video');
                                    videos.forEach(function(v) {
                                        var src = v.currentSrc || v.src;
                                        var poster = v.poster || '';
                                        if (src && !src.startsWith('blob:')) {
                                            AndroidSniffer.onMediaFound(src, document.title, poster);
                                        }
                                    });

                                    // Check video source tags
                                    var sources = document.querySelectorAll('video source');
                                    sources.forEach(function(s) {
                                        if (s.src) {
                                            AndroidSniffer.onMediaFound(s.src, document.title, '');
                                        }
                                    });

                                    // Check og:video meta tags
                                    var metaVideo = document.querySelector('meta[property="og:video"]') || document.querySelector('meta[property="og:video:url"]');
                                    if (metaVideo && metaVideo.content) {
                                        var metaImage = document.querySelector('meta[property="og:image"]');
                                        var poster = metaImage ? metaImage.content : '';
                                        AndroidSniffer.onMediaFound(metaVideo.content, document.title, poster);
                                    }
                                }

                                checkMedia();
                                setInterval(checkMedia, 2000);
                            })();
                        """.trimIndent()

                        view?.evaluateJavascript(snifferScript, null)
                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val reqUrl = request?.url?.toString() ?: ""
                        if (reqUrl.contains(".mp4") || reqUrl.contains(".m3u8") || reqUrl.contains(".webm") || reqUrl.contains(".mp3")) {
                            onMediaSniffed(reqUrl, "Detected Media Stream", "")
                        }
                        return super.shouldInterceptRequest(view, request)
                    }
                }

                onWebViewCreated(this)
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url && url.isNotBlank()) {
                webView.loadUrl(url)
            }
        }
    )
}
