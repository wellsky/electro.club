package club.electro.utils

import androidx.core.text.HtmlCompat

class StripTags(val html: String) {
    override fun toString(): String {
        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    }
}

