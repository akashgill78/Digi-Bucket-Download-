package com.example.ui.screens

import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.browser.SnifferWebView
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandOnPrimaryContainer
import com.example.ui.theme.BrandOnSecondaryContainer
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandPrimaryContainer
import com.example.ui.theme.BrandSecondaryContainer
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BrowserScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val browserUrl by viewModel.browserUrl.collectAsState()
    val sniffedCount by viewModel.sniffedMediaCount.collectAsState()

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var currentUrlDisplay by remember { mutableStateOf(browserUrl) }
    var pageProgress by remember { mutableIntStateOf(100) }

    val socialShortcuts = listOf(
        "Facebook" to "https://m.facebook.com",
        "Instagram" to "https://www.instagram.com",
        "YouTube" to "https://m.youtube.com",
        "Twitter" to "https://x.com",
        "TikTok" to "https://www.tiktok.com",
        "Pinterest" to "https://www.pinterest.com",
        "Reddit" to "https://www.reddit.com"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PolishBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Browser Bar & Address Row
            Surface(
                color = PolishSurface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishBorder.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (webViewRef?.canGoBack() == true) webViewRef?.goBack()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextSecondary
                            )
                        }

                        IconButton(
                            onClick = {
                                if (webViewRef?.canGoForward() == true) webViewRef?.goForward()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Forward",
                                tint = TextSecondary
                            )
                        }

                        // URL Address Bar
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = PolishSurfaceVariant
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Secure",
                                    tint = BrandEmerald,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentUrlDisplay.replace("https://", "").replace("http://", ""),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        IconButton(
                            onClick = { webViewRef?.reload() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = TextSecondary
                            )
                        }
                    }

                    // Social Shortcuts Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        socialShortcuts.forEach { (name, url) ->
                            val isSelected = currentUrlDisplay.contains(url.replace("https://m.", "").replace("https://www.", "").take(6))
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) BrandPrimary else PolishSurfaceVariant,
                                modifier = Modifier.clickable {
                                    viewModel.navigateBrowser(url)
                                    webViewRef?.loadUrl(url)
                                }
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color.White else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Web Loading Progress
            if (pageProgress < 100) {
                LinearProgressIndicator(
                    progress = { pageProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = BrandPrimary,
                    trackColor = Color.Transparent
                )
            }

            // Embedded Sniffer WebView
            SnifferWebView(
                url = browserUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onPageStarted = { url ->
                    currentUrlDisplay = url
                },
                onPageFinished = { url ->
                    currentUrlDisplay = url
                    pageProgress = 100
                },
                onProgressChanged = { prog ->
                    pageProgress = prog
                },
                onMediaSniffed = { mediaUrl, pageTitle, thumb ->
                    viewModel.onBrowserMediaSniffed(mediaUrl, pageTitle, thumb)
                },
                onWebViewCreated = { wv ->
                    webViewRef = wv
                }
            )
        }

        // Floating Pulsating Download Button (One-Click Sniffer)
        PulsatingSnifferFab(
            detectedCount = if (sniffedCount > 0) sniffedCount else 1,
            onClick = {
                viewModel.triggerSnifferDownload()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 85.dp)
        )
    }
}

@Composable
fun PulsatingSnifferFab(
    detectedCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
    ) {
        // Quick Sniffer Tooltip
        Surface(
            shape = CircleShape,
            color = BrandOnPrimaryContainer,
            modifier = Modifier.padding(bottom = 6.dp),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Sniffer Active",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "1-Click Download Ready",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .scale(pulseScale)
                .size(60.dp)
                .background(BrandPrimary, CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Download Video Now",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )

            // Detected items badge counter
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(20.dp)
                    .background(BrandPrimaryContainer, CircleShape)
                    .border(1.dp, BrandOnPrimaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$detectedCount",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = BrandOnPrimaryContainer
                    )
                )
            }
        }
    }
}
