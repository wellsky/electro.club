package club.electro.utils

import androidx.core.text.HtmlCompat
import club.electro.adapter.PostEntityContentPreparator
import club.electro.dto.Post
import club.electro.dto.User
import kotlinx.coroutines.runBlocking

// TODO надо объединить с PostEntityPreparator, вынести в отдельный модуль
fun String.toPlainText(): String {
    return this
        .removeTags()
        .removeHtml()
        .trim()
}

fun String.removeHtml(): String {
    return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
}

fun String.removeTags(): String {
    return this
        .removeTransportTags()
        .removeSpoilerTags()
        .removeMessageInSpoilerTags()
        .removeUsersTags()
        .removeQuotesTags()
}

private fun String.removeSpoilerTags(): String {
    val pattern1 = """\[spoiler="(.*?)"\](.*?)\[\/spoiler\]"""

    val result1 = Regex(pattern1).replace(this) {
        runBlocking {
            val (spoilerTitle, spoilerContent) = it.destructured
            spoilerTitle
        }
    }

    val pattern2 = """\[spoiler\](.*?)\[\/spoiler\]"""
    val result2 = Regex(pattern2).replace(result1) {
        val (spoilerContent) = it.destructured
        ""
    }

    return result2
}

private fun String.removeMessageInSpoilerTags(): String {
    val pattern = """\[message=(.*?)\ spoiler="(.*?)"]"""

    val result = Regex(pattern).replace(this) {
        val (messageId, spoilerTitle) = it.destructured
        spoilerTitle
    }

    return result
}

private fun String.removeTransportTags(): String {
    var newText = this

    val pattern1 = """\[transport_image=(.*?)\]"""
    newText = Regex(pattern1).replace(newText) {
        runBlocking {
            val (transportId) = it.destructured
            ""
        }
    }

    val pattern2 = """\[transport_links=(.*?)\]"""
    newText = Regex(pattern2).replace(newText) {
        runBlocking {
            val (transportId) = it.destructured
            ""
        }
    }

    return newText
}

private fun String.removeUsersTags(): String {
    val pattern = """\[user=(\d+?)\]"""

    val result = Regex(pattern).replace(this) {
        runBlocking {
            val (userId) = it.destructured
            "\uD83D\uDC64"
        }
    }

    return result
}

private fun String.removeQuotesTags(): String {
    val pattern = """\[quote message=(\d+?)\](.*?)\[\/quote\]"""
    val result = Regex(pattern).replace(this) {
        "\uD83D\uDCCB"
    }

    return result
}