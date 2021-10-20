package club.electro.utils

fun trimWhiteSpaces(s: CharSequence): CharSequence {
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