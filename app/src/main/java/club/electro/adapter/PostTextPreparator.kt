package club.electro.adapter

import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import club.electro.di.DependencyContainer
import club.electro.dto.FeedPost
import club.electro.dto.Post
import club.electro.dto.User
import club.electro.repository.PostRepository
import club.electro.repository.UserRepository
import kotlinx.coroutines.runBlocking

class MultiplePostsTextPreparator(val posts: List<Post>) {
    val diContainer = DependencyContainer.getInstance()
    val postRepository: PostRepository = diContainer.postRepository

    suspend fun prepareAll(): List<Post> {
        println("Prepare all")
        return posts.map {
            val post = it.copy( preparedContent = PostTextPreparator(it) {
                val updatedPsot = it.copy(preparedContent = PostTextPreparator(it).prepareAll().get())
                postRepository.savePostToChache(updatedPsot)
            }.prepareAll().get())

            if (post.id == 31921L) {
                println("31921!!!!")
            }
            post
        }
    }
}

class PostTextPreparator(source: String, val answerTo: Long? = null, val callback: suspend () -> Unit = {}) {
    val ANSWERTO_MAX_TEXT_LENGTH = 128

    var text = source

    val diContainer = DependencyContainer.getInstance()
    val postRepository: PostRepository = diContainer.postRepository


    val userRepository: UserRepository = diContainer.userRepository


    constructor(post: Post, callback: suspend () -> Unit = {}): this(post.content, post.answerTo, callback)
    constructor(post: FeedPost, callback: suspend () -> Unit = {}): this(post.content, null, callback)

    fun get(): String {
        return text
    }

    suspend fun prepareAll(): PostTextPreparator {
        prepareLegacyQuotes()
        prepareEmojies()
        prepareImagesOnNewLine()
        prepareBasicTags()
        prepareRelativeUrls()

        // suspend:
        prepareAnswerTo()
        prepareQuotes()
        prepareUsers()

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
            val sourceMessage = postRepository.getLocalPostById(it)
            val answerText = sourceMessage?.let {
                "<blockquote><strong>Ответ " + sourceMessage.authorName + "</strong>: " + shortTextPreview(sourceMessage.content) + "</blockquote>"
            } ?: "<blockquote><strong>Ответ на сообщение " + it + "</strong></blockquote>"
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
                val sourceMessage = postRepository.getLocalPostById(quotedMessageId.toLong())
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
     * Заменяет тэги [quote messge=<id>]<text>[/quote] на цитату
     */
    suspend fun prepareUsers(): PostTextPreparator {
        val pattern = """\[user=(\d+?)\]"""

        val result = Regex(pattern).replace(text) {
            runBlocking {
                val (userId) = it.destructured
                val author: User? = userRepository.getLocalById(userId.toLong()) {
                    runBlocking {
                        callback()
                    }
                } // userDao.getUserById(userId.toLong())?.toDto()
                //val author = ("<strong>" + sourceMessage?.authorName ?: "?") + "</strong>: "
                "<a href=\"https://electro.club/users/" + userId + "\">@" + (author?.name
                    ?: "@user" + userId) + "</a>"
            }
        }

        text = result
        return this
    }

    /**
     * Заменяет тэги [quote="<authorName>"]<text>[/quote] и [quote ]<text>[/quote] на цитату
     */
    fun prepareLegacyQuotes(): PostTextPreparator {
        val pattern1= """\[quote="(.*?)"\](.*?)\[\/quote\]"""

        val result1 = Regex(pattern1).replace(text) {
            runBlocking {
                val (quotedMessageAuthor, quoteText) = it.destructured
                val author = "<strong>" + quotedMessageAuthor + "</strong>: "
                "<blockquote>" + author + quoteText + "</blockquote>"
            }
        }

        val pattern2= """\[quote\](.*?)\[\/quote\]"""
        val result2 = Regex(pattern2).replace(result1) {
            runBlocking {
                val (quoteText) = it.destructured
                val author = "<strong>Цитата: </strong>: "
                "<blockquote>" + author + quoteText + "</blockquote>"
            }
        }

        text = result2
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