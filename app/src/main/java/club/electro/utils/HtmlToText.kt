package club.electro.utils

import androidx.core.text.HtmlCompat

fun HtmlToText(html: String): String {
    return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
}


