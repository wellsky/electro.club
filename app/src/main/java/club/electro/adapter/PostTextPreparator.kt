package club.electro.adapter

import club.electro.di.DependencyContainer
import club.electro.repository.PostRepository
import club.electro.repository.ThreadRepository
import kotlinx.coroutines.runBlocking

class PostTextPreparator(source: String) {
    var text = source

    val diContainer = DependencyContainer.getInstance()
    val repository: PostRepository = diContainer.postRepository

    fun get(): String {
        return text
    }

    suspend fun prepareAll(): PostTextPreparator {
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