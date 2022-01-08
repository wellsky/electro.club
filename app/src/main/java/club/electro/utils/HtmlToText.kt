package club.electro.utils

import androidx.core.text.HtmlCompat

fun htmlToText(html: String): String {
    return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
}


