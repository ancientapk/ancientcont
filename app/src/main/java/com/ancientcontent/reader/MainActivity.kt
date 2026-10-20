package com.ancientcontent.reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ancientcontent.reader.ui.AncientContentTheme
import com.ancientcontent.reader.ui.FeedViewModel
import com.ancientcontent.reader.ui.ListScreen
import com.ancientcontent.reader.ui.ReaderScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AncientContentTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    App()
                }
            }
        }
    }
}

@Composable
private fun App(viewModel: FeedViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var openId by rememberSaveable { mutableStateOf<String?>(null) }
    val openPost = state.posts.firstOrNull { it.id == openId }

    if (openPost != null) {
        BackHandler { openId = null }
        ReaderScreen(
            post = openPost,
            saved = openPost.id in state.saved,
            onBack = { openId = null },
            onToggleSaved = { viewModel.toggleSaved(openPost) },
        )
    } else {
        ListScreen(
            state = state,
            onRefresh = viewModel::refresh,
            onOpen = { openId = it.id },
            onToggleSaved = viewModel::toggleSaved,
        )
    }
}
