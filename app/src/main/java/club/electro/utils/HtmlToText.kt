package club.electro.utils

import androidx.core.text.HtmlCompat
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