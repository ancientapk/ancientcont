package com.ancientcontent.reader.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ancientcontent.reader.data.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    post: Post,
    saved: Boolean,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val html = remember(post.id, colors.background, colors.onBackground) {
        buildHtml(
            post = post,
            background = colors.background.css(),
            text = colors.onBackground.css(),
            accent = colors.primary.css(),
            muted = colors.onSurfaceVariant.css(),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleSaved) {
                        Icon(
                            imageVector = if (saved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (saved) "Remove from saved" else "Save",
                        )
                    }
                    IconButton(onClick = { share(context, post) }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                },
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (post.isPreview) {
                PreviewBanner(onOpen = { openInBrowser(context, post.link) })
            }
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        settings.javaScriptEnabled = false
                        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView,
                                request: WebResourceRequest,
                            ): Boolean {
                                openInBrowser(ctx, request.url.toString())
                                return true
                            }
                        }
                    }
                },
                update = { view ->
                    if (view.tag != html) {
                        view.tag = html
                        view.loadDataWithBaseURL(
                            "https://www.ancientcontent.com",
                            html,
                            "text/html",
                            "utf-8",
                            null,
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun PreviewBanner(onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "This post is a preview. The full article is for paid subscribers.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onOpen) { Text("Open on Substack") }
        }
    }
}

private fun Color.css(): String = String.format("#%06X", 0xFFFFFF and toArgb())

private fun buildHtml(
    post: Post,
    background: String,
    text: String,
    accent: String,
    muted: String,
): String {
    val title = TextUtils.htmlEncode(post.title)
    val date = formatDate(post.publishedAt)
    return """
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
body{margin:0;padding:16px 18px 48px;background:$background;color:$text;font-family:Georgia,serif;font-size:18px;line-height:1.7;word-wrap:break-word}
h1{font-size:26px;line-height:1.25;margin:8px 0 6px}
h2,h3{line-height:1.3;margin:1.6em 0 .5em}
.meta{color:$muted;font-size:14px;margin-bottom:18px;font-family:sans-serif}
img{max-width:100%;height:auto;border-radius:6px}
figure{margin:18px 0}
a{color:$accent}
h5,h6,figcaption{color:$muted;font-size:13px;font-weight:normal;line-height:1.5;font-family:sans-serif;margin:8px 0}
hr{border:none;border-top:1px solid $muted;opacity:.3;margin:28px 0}
blockquote{border-left:3px solid $accent;margin:1em 0;padding-left:14px}
.subscription-widget-wrap-editor,.image-link-expand,.restack-image,.view-image,.native-audio-embed{display:none}
.button-wrapper{text-align:center;margin:24px 0}
a.button{display:inline-block;padding:12px 22px;border-radius:8px;background:$accent;color:$background;text-decoration:none;font-family:sans-serif;font-weight:bold}
</style>
</head>
<body>
<h1>$title</h1>
<div class="meta">$date</div>
${post.html}
</body>
</html>
""".trimIndent()
}

private fun share(context: Context, post: Post) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "${post.title}\n${post.link}")
    }
    context.startActivity(Intent.createChooser(send, null))
}

private fun openInBrowser(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        // No browser installed. Nothing else to do.
    }
}
