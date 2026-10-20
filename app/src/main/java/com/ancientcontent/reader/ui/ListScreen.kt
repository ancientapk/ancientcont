package com.ancientcontent.reader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ancientcontent.reader.data.Post
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    state: UiState,
    onRefresh: () -> Unit,
    onOpen: (Post) -> Unit,
    onToggleSaved: (Post) -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val visible = if (tab == 0) state.posts else state.posts.filter { it.id in state.saved }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ancient Content", style = MaterialTheme.typography.titleLarge) }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Latest") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Saved") })
            }

            if (state.offline) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Offline. Showing posts saved on this phone.",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (visible.isEmpty()) {
                        item {
                            Text(
                                text = emptyMessage(tab, state),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 32.dp),
                            )
                        }
                    } else {
                        items(visible, key = { it.id }) { post ->
                            PostCard(
                                post = post,
                                saved = post.id in state.saved,
                                onOpen = { onOpen(post) },
                                onToggleSaved = { onToggleSaved(post) },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun emptyMessage(tab: Int, state: UiState): String = when {
    tab == 1 -> "Posts you save appear here and stay available offline."
    state.refreshing -> "Loading posts."
    state.offline -> "No posts on this phone yet. Connect to the internet and pull down to refresh."
    else -> "No posts found."
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostCard(
    post: Post,
    saved: Boolean,
    onOpen: () -> Unit,
    onToggleSaved: () -> Unit,
) {
    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            if (post.imageUrl != null) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1.91f),
                )
            }
            Column(Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 4.dp)) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = post.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = formatDate(post.publishedAt) + if (post.isPreview) "  |  Paid post" else "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onToggleSaved) {
                        Icon(
                            imageVector = if (saved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (saved) "Remove from saved" else "Save",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

internal fun formatDate(millis: Long): String =
    if (millis <= 0L) "" else DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(millis))
