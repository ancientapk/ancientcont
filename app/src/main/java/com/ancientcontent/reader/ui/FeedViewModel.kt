package com.ancientcontent.reader.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ancientcontent.reader.data.BookmarkStore
import com.ancientcontent.reader.data.FeedRepository
import com.ancientcontent.reader.data.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val posts: List<Post> = emptyList(),
    val saved: Set<String> = emptySet(),
    val refreshing: Boolean = false,
    val offline: Boolean = false,
)

class FeedViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = FeedRepository(app)
    private val bookmarks = BookmarkStore(app)

    private val _state = MutableStateFlow(
        UiState(posts = repository.loadCached(), saved = bookmarks.all())
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true) }
            try {
                val posts = repository.refresh()
                _state.update { it.copy(posts = posts, refreshing = false, offline = false) }
            } catch (e: Exception) {
                _state.update { it.copy(refreshing = false, offline = true) }
            }
        }
    }

    fun toggleSaved(post: Post) {
        _state.update { it.copy(saved = bookmarks.toggle(post.id)) }
    }
}
