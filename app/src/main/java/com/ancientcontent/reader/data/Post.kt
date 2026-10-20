package com.ancientcontent.reader.data

data class Post(
    val id: String,
    val title: String,
    val link: String,
    val summary: String,
    val publishedAt: Long,
    val imageUrl: String?,
    val html: String,
    /** True when the feed only carries a preview because the post is for paid subscribers. */
    val isPreview: Boolean,
)
