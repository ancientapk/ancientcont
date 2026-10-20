package com.ancientcontent.reader.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class FeedRepository(context: Context) {

    private val cacheFile = File(context.filesDir, "feed_cache.json")

    /** Posts saved on the phone. Works with no connection. */
    fun loadCached(): List<Post> {
        if (!cacheFile.exists()) return emptyList()
        return try {
            val array = JSONArray(cacheFile.readText())
            List(array.length()) { array.getJSONObject(it).toPost() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Downloads the feed, merges it with the saved posts and stores the result. */
    suspend fun refresh(): List<Post> = withContext(Dispatchers.IO) {
        val connection = (URL(FEED_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("User-Agent", "AncientContentReader/0.1")
        }
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${connection.responseCode}")
            }
            val fresh = connection.inputStream.use { FeedParser.parse(it) }
            if (fresh.isEmpty()) throw IOException("Empty feed")

            val merged = (fresh + loadCached())
                .distinctBy { it.id }
                .sortedByDescending { it.publishedAt }
                .take(MAX_POSTS)
            save(merged)
            merged
        } finally {
            connection.disconnect()
        }
    }

    private fun save(posts: List<Post>) {
        val array = JSONArray()
        posts.forEach { array.put(it.toJson()) }
        val temp = File(cacheFile.parentFile, "feed_cache.tmp")
        temp.writeText(array.toString())
        temp.renameTo(cacheFile)
    }

    private fun Post.toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("link", link)
        .put("summary", summary)
        .put("publishedAt", publishedAt)
        .put("imageUrl", imageUrl ?: JSONObject.NULL)
        .put("html", html)
        .put("isPreview", isPreview)

    private fun JSONObject.toPost(): Post = Post(
        id = getString("id"),
        title = getString("title"),
        link = getString("link"),
        summary = getString("summary"),
        publishedAt = getLong("publishedAt"),
        imageUrl = if (isNull("imageUrl")) null else getString("imageUrl"),
        html = getString("html"),
        isPreview = getBoolean("isPreview"),
    )

    companion object {
        const val FEED_URL = "https://www.ancientcontent.com/feed"
        private const val MAX_POSTS = 60
    }
}
