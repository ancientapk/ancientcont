package com.ancientcontent.reader.data

import android.text.Html
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale

object FeedParser {

    private val dateFormat
        get() = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)

    fun parse(stream: InputStream): List<Post> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(stream, null)

        val posts = mutableListOf<Post>()
        var inItem = false
        var title = ""
        var link = ""
        var guid = ""
        var description = ""
        var pubDate = ""
        var content = ""
        var image: String? = null

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    val name = parser.name
                    if (name == "item") {
                        inItem = true
                        title = ""; link = ""; guid = ""; description = ""
                        pubDate = ""; content = ""; image = null
                    } else if (inItem) {
                        when (name) {
                            "title" -> title = parser.nextText()
                            "link" -> link = parser.nextText()
                            "guid" -> guid = parser.nextText()
                            "description" -> description = parser.nextText()
                            "pubDate" -> pubDate = parser.nextText()
                            "content:encoded" -> content = parser.nextText()
                            "enclosure" -> image = parser.getAttributeValue(null, "url")
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "item" && inItem) {
                        inItem = false
                        val id = guid.ifBlank { link }
                        if (id.isNotBlank() && title.isNotBlank()) {
                            posts += Post(
                                id = id,
                                title = plain(title),
                                link = link,
                                summary = plain(description),
                                publishedAt = parseDate(pubDate),
                                imageUrl = image,
                                html = content.ifBlank { "<p>${description}</p>" },
                                isPreview = looksLikePreview(content),
                            )
                        }
                    }
                }
            }
            event = parser.next()
        }
        return posts
    }

    /** Substack ends paywalled previews with a "Read more" link. Free posts do not have it. */
    private fun looksLikePreview(html: String): Boolean =
        html.takeLast(700).contains("Read more", ignoreCase = true)

    private fun plain(text: String): String =
        Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString().trim()

    private fun parseDate(value: String): Long =
        try {
            dateFormat.parse(value.trim())?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
}
