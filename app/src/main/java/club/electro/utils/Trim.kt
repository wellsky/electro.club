package club.electro.utils
import androidx.core.text.HtmlCompat

fun trim(s: CharSequence): CharSequence {
    var start = 0
    var end = s.length
    while (start < end && Character.isWhitespace(s.elementAt(start))) {
        start++;
    }

    while (end > start && Character.isWhitespace(s.elementAt(end - 1))) {
        end--;
    }

    return s.subSequence(start, end);
}