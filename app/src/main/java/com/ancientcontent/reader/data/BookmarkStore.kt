package com.ancientcontent.reader.data

import android.content.Context

class BookmarkStore(context: Context) {

    private val prefs = context.getSharedPreferences("bookmarks", Context.MODE_PRIVATE)

    fun all(): Set<String> = prefs.getStringSet(KEY, emptySet())?.toSet() ?: emptySet()

    /** Adds or removes the post and returns the updated set. */
    fun toggle(id: String): Set<String> {
        val updated = all().toMutableSet()
        if (!updated.add(id)) updated.remove(id)
        prefs.edit().putStringSet(KEY, updated).apply()
        return updated
    }

    private companion object {
        const val KEY = "saved_ids"
    }
}
