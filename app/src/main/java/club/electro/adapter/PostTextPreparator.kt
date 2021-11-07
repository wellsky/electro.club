package club.electro.adapter

import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import club.electro.di.DependencyContainer
import club.electro.dto.FeedPost
import club.electro.dto.Post
import club.electro.repository.PostRepository
import club.electro.repository.ThreadRepository
import kotlinx.coroutines.runBlocking

class PostTextPreparator(source: String, val answerTo: Long? = null) {
    val ANSWERTO_MAX_TEXT_LENGTH = 128

    var text = source

    val diContainer = DependencyContainer.getInstance()
    val repository: PostRepository = diContainer.postRepository

    constructor(post: Post): this(post.content, post.answerTo)
    constructor(post: FeedPost): this(post.content)

    fun get(): String {
        return text
    }

    suspend fun prepareAll(): PostTextPreparator {
        prepareAnswerTo()
        prepareQuotes()
        prepareEmojies()
        prepareImagesOnNewLine()

        prepareBasicTags()
        prepareRelativeUrls()

        return this
    }


    fun prepareBasicTags(): PostTextPreparator {
        var newText = text
        newText = newText.replace("<br /></p>", "</p>")
        newText = newText.replace("<p>", "")
        newText = newText.replace("</p>", "<br>")

        text = newText
        return this
    }

    suspend fun prepareAnswerTo(): PostTextPreparator {
        fun shortTextPreview(content: String): String {
            val noHtmlTags = HtmlCompat.fromHtml(content, FROM_HTML_MODE_LEGACY).toString()

            return if (noHtmlTags.length > ANSWERTO_MAX_TEXT_LENGTH) {
                noHtmlTags.substring(0, ANSWERTO_MAX_TEXT_LENGTH) + "..."
            } else {
                noHtmlTags
            }
        }

        answerTo?.let {
            val sourceMessage = repository.getLocalPostById(it)
            val answerText = sourceMessage?.let {
                "<blockquote><strong>Ответ " + sourceMessage.authorName + "</strong>: " + shortTextPreview(sourceMessage.content) + "</blockquote>"
            } ?: "<blockquote><strong>Ответ на неизвестное сообщение</strong></blockquote>"
            text = answerText + text
        }
        return this
    }

    fun preparePlainText(): PostTextPreparator {
        var newText = text
        newText = newText.replace("<br>", "\n")
        newText = newText.replace("<br />", "\n")

        text = newText
        return this
    }

    fun prepareRelativeUrls(): PostTextPreparator {
        var newText = text
        newText = newText.replace("src=\"/data/", "src=\"https://electro.club/data/")

        text = newText
        return this
    }

    /**
     * Заменяет тэги [quote messge=<id>]<text>[/quote] на цитату
     */
    suspend fun prepareQuotes(): PostTextPreparator {
        val pattern = """\[quote message=(\d+?)\](.*?)\[\/quote\]"""

        val result = Regex(pattern).replace(text) {
            runBlocking {
                val (quotedMessageId, quoteText) = it.destructured
                val sourceMessage = repository.getLocalPostById(quotedMessageId.toLong())
                val author = ("<strong>" + sourceMessage?.authorName ?: "?") + "</strong>: "
                "<blockquote>" + author + quoteText + "</blockquote>"
            }
        }

//        val lamda: (it: MatchResult) -> CharSequence = {
//            val (quotedMessageId, quoteText) = it.destructured
//
//            val sourceMessage = GlobalScope.async {
//                repository.getPostById(quotedMessageId.toLong())
//            }
//
//            val author = ("<strong>" + sourceMessage.authorName ?: "?") + "</strong>: "
//            "<blockquote>" +author + quoteText + "</blockquote>"
//        }
//        val result = Regex(pattern).replace(text, lamda)

        text = result
        return this
    }

    /**
     * В текстах встречаются изображения с классом emojione
     * Вместо этих изображений надо подставить эмоджи, который указан у них в alt
     */
    fun prepareEmojies(): PostTextPreparator {
        val pattern = """<img class="emojione".*?alt="(.*?)"[^\>]+>"""
        val result = Regex(pattern).replace(text) {
            val (emojieChar) = it.destructured
            emojieChar
        }
        text = result
        return this
    }


    fun prepareImagesOnNewLine() : PostTextPreparator {
        var newText = text
        newText = newText.replace("<img", "<br><img")

        text = newText
        return this
    }
}