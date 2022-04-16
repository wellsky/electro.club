package club.electro.adapter

import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import club.electro.BuildConfig
import club.electro.dto.Post
import club.electro.dto.User
import club.electro.entity.PostEntity
import club.electro.repository.post.PostRepository
import club.electro.repository.user.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.runBlocking

class PostsEntitiesPreparator @AssistedInject constructor(
    @Assisted
    private val postsEntities: List<PostEntity>,
    @Assisted("onStart")
    private val onStart: (suspend (postsEntities: List<PostEntity>) -> Unit) = { },
    @Assisted("onFirstResult")
    private val onFirstResult: (suspend (postsEntities: List<PostEntity>) -> Unit) = { },
    @Assisted
    private val onEveryDataUpdate: (suspend (postEntity: PostEntity) -> Unit) = { },
    private val postEntityContentPreparatorFactory: PostEntityContentPreparator.Factory,
) {

    @AssistedFactory
    interface Factory {
        fun create(
            postsEntities: List<PostEntity>,
            @Assisted("onStart")
            onStart: (suspend (postsEntities: List<PostEntity>) -> Unit) = { },
            @Assisted("onFirstResult")
            onFirstResult: (suspend (postsEntities: List<PostEntity>) -> Unit) = { },
            onEveryDataUpdate: (suspend (postEntity: PostEntity) -> Unit) = { },
        ): PostsEntitiesPreparator
    }

    suspend fun prepareAll() {
        onStart(postsEntities)
        val preparedEntities = postsEntities.map {
            postEntityContentPreparatorFactory.create(
                postEntity = it,
                onEveryDataUpdate = {
                    onEveryDataUpdate(
                        postEntityContentPreparatorFactory.create(
                            postEntity = it,
                        )
                            .prepareAll()
                            .getPrepared()
                    )
                }
            ).prepareAll().getPrepared()
        }
        onFirstResult(preparedEntities)
    }
}


class PostEntityContentPreparator @AssistedInject constructor(
    @Assisted
    private val postEntity: PostEntity,
    @Assisted
    private val onEveryDataUpdate: (suspend () -> Unit)? = null,

    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
) {
    @AssistedFactory
    interface Factory {
        fun create(
            postEntity: PostEntity,
            onEveryDataUpdate: (suspend () -> Unit)? = null,
        ): PostEntityContentPreparator
    }

    val ANSWERTO_MAX_TEXT_LENGTH = 128

    var text = postEntity.content

    fun getPrepared(): PostEntity {
        return postEntity.copy(preparedContent = text)
    }

    suspend fun prepareAll(): PostEntityContentPreparator {
        prepareLegacyQuotes()
        prepareLegacyUrls()
        prepareEmojies()
        prepareImagesOnNewLine()
        prepareBasicTags()
        prepareRelativeUrls()
        prepareAppVersionTag()

        // suspend:
        prepareAnswerTo()
        prepareQuotes()
        prepareUsers()

        return this
    }


    private fun prepareBasicTags(): PostEntityContentPreparator {
        var newText = text
        //newText = newText.replace("<br /></p>", "</p>")
        //newText = newText.replace("<p>", "")
        //newText = newText.replace("</p>", "<br>")

        text = newText
        return this
    }

    private suspend fun prepareAnswerTo(): PostEntityContentPreparator {
        fun shortTextPreview(content: String): String {
            val noHtmlTags = HtmlCompat.fromHtml(content, FROM_HTML_MODE_LEGACY).toString()

            return if (noHtmlTags.length > ANSWERTO_MAX_TEXT_LENGTH) {
                noHtmlTags.substring(0, ANSWERTO_MAX_TEXT_LENGTH) + "..."
            } else {
                noHtmlTags
            }
        }

        postEntity?.answerTo?.let {
            val sourceMessage: Post? = postRepository.getLocalById(
                postEntity.threadType,
                postEntity.threadId,
                it,
                onEveryDataUpdate
            )
            val answerText = sourceMessage?.let {
                "<blockquote><strong>Ответ ${sourceMessage.authorName} </strong>: ${shortTextPreview(sourceMessage.content)}</blockquote>"
            } ?: "<blockquote><strong>Ответ на сообщение $it</strong></blockquote>"
            text = answerText + text
        }
        return this
    }

    /**
     * Тэг, отображающий номер версии
     */
    private fun prepareAppVersionTag(): PostEntityContentPreparator {
        var newText = text
        newText = newText.replace("[app_version]", BuildConfig.VERSION_NAME)
        text = newText
        return this
    }

    private fun preparePlainText(): PostEntityContentPreparator {
        var newText = text
        newText = newText.replace("<br>", "\n")
        newText = newText.replace("<br />", "\n")

        text = newText
        return this
    }

    /**
     * Если в тексте изображение имеет относительный URL, добавляет полностю домен
     */
    private fun prepareRelativeUrls(): PostEntityContentPreparator {
        var newText = text
        newText = newText.replace("src=\"/data/", "src=\"https://electro.club/data/")

        text = newText
        return this
    }

    /**
     * Заменяет тэги [quote messge=<id>]<text>[/quote] на цитату
     */
    private suspend fun prepareQuotes(): PostEntityContentPreparator {
        postEntity?.let { post ->
            val pattern = """\[quote message=(\d+?)\](.*?)\[\/quote\]"""
            val result = Regex(pattern).replace(text) {
                runBlocking {
                    val (quotedMessageId, quoteText) = it.destructured
                    val sourceMessage: Post? = postRepository.getLocalById(
                        post.threadType,
                        post.threadId,
                        quotedMessageId.toLong(),
                        onEveryDataUpdate
                    )
                    val author = "<strong>${sourceMessage?.authorName ?: "?"}</strong>:"
                    "<blockquote>$author $quoteText</blockquote>"
                }
            }

            text = result
        }
        return this
    }


    /**
     * Заменяет тэги [user=<id>] на никнейм, ссылающийся на профиль пользователя
     */
    private suspend fun prepareUsers(): PostEntityContentPreparator {
        val pattern = """\[user=(\d+?)\]"""

        val result = Regex(pattern).replace(text) {
            runBlocking {
                val (userId) = it.destructured
                val author: User? = userRepository.getLocalById(userId.toLong(), onLoadedCallback = onEveryDataUpdate)
                "<a href=\"https://electro.club/users/$userId\">@${author?.name ?: "user" + userId}</a>"
            }
        }

        text = result
        return this
    }

    /**
     * Заменяет тэги [quote="<authorName>"]<text>[/quote] и [quote ]<text>[/quote] на цитату
     */
    private fun prepareLegacyQuotes(): PostEntityContentPreparator {
        val pattern1= """\[quote="(.*?)"\](.*?)\[\/quote\]"""

        val result1 = Regex(pattern1).replace(text) {
            runBlocking {
                val (quotedMessageAuthor, quoteText) = it.destructured
                val author = "<strong>$quotedMessageAuthor</strong>:"
                "<blockquote>$author $quoteText</blockquote>"
            }
        }

        val pattern2= """\[quote\](.*?)\[\/quote\]"""
        val result2 = Regex(pattern2).replace(result1) {
            val (quoteText) = it.destructured
            val author = "<strong>Цитата: </strong>:"
            "<blockquote>$author $quoteText</blockquote>"
        }

        text = result2
        return this
    }

    private fun prepareLegacyUrls(): PostEntityContentPreparator {
        val pattern= """\[url=(.*?)\](.*?)\[\/url\]"""

        val result = Regex(pattern).replace(text) {
            val (url, anchor) = it.destructured
            "<a href=\"$url\">$anchor</a>"
        }

        text = result
        return this
    }


    /**
     * В текстах встречаются изображения с классом emojione
     * Вместо этих изображений надо подставить эмоджи, который указан у них в alt
     */
    private fun prepareEmojies(): PostEntityContentPreparator {
        val pattern = """<img class="emojione".*?alt="(.*?)"[^\>]+>"""
        val result = Regex(pattern).replace(text) {
            val (emojieChar) = it.destructured
            emojieChar
        }
        text = result
        return this
    }


    private fun prepareImagesOnNewLine() : PostEntityContentPreparator {
        var newText = text
        newText = newText.replace("<img", "<br><img")

        text = newText
        return this
    }
}