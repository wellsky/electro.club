package club.electro.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavController
import club.electro.R
import club.electro.api.ApiService
import club.electro.dto.ThreadType
import club.electro.error.ApiError
import club.electro.repository.thread.ThreadLoadTarget.Companion.TARGET_POSITION_FIRST
import club.electro.ui.thread.ThreadFragment.Companion.targetPostId
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import club.electro.ui.user.UserProfileFragment.Companion.userId
import com.google.gson.TypeAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI


class UrlHandler @AssistedInject constructor(
    @Assisted val urlHandlerAction: UrlHandlerAction,
    val apiService: ApiService
) {
    @AssistedFactory
    interface Factory {
        fun create(urlHandlerAction: UrlHandlerAction): UrlHandler
    }

    companion object {
        private const val PRIMARY_HOST = "electro.club"
        private const val PATH_USERS = "users"
    }

    private var url: String = ""

    fun setUrl(url: String): UrlHandler {
        this.url = url
        return this
    }

    fun open() {
        val uri = URI(url)
        val host: String = uri.host

        if (host == PRIMARY_HOST) {
            parseUriLocal(uri)
                ?.let(UrlDataResultDto::toDomainModel)
                ?.let(this::openUrlData)
                ?: parseUriRemoteAndOpen(uri)
        } else {
            urlHandlerAction.openUnknown(uri.toString())
        }
    }

    private fun parseUriLocal(uri: URI): UrlDataResultDto? {
        val path: String = uri.path
        if (path.isNotBlank()) {
            val firstPath = path.substringAfter("/").substringBefore("/")

            when (firstPath) {
                PATH_USERS -> {
                    val urlUserId = path.substringAfter("/").substringAfter("/").toLong()

                    return UrlDataResultDto(
                        type = UrlType.URL_TYPE_USER_ACCOUNT,
                        userId = urlUserId
                    )
                }
            }
        }
        return null
    }


    private fun parseUriRemoteAndOpen(uri: URI) {
        CoroutineScope(Dispatchers.Main).launch {
            val response = apiService.getUrlData(
                url = uri.toString()
            )

            val result = response.body()
                ?.data
                ?.let(UrlDataResultDto::toDomainModel)
                ?: throw ApiError(response.code(), response.message())

            openUrlData(result)
        }
    }

    private fun openUrlData(data: UrlDataResult) {
        when (data) {
            is UrlDataResult.Thread -> {
                urlHandlerAction.openThread(data)
            }
            is UrlDataResult.MessageInThread -> {
                urlHandlerAction.openMessageInThread(data)
            }
            is UrlDataResult.UserAccount -> {
                urlHandlerAction.openUserAccount(data)
            }
            is UrlDataResult.Unknown -> {
                urlHandlerAction.openUnknown(url.toString())
            }
        }
    }
}


open class UrlHandlerAction(
    private val navController: NavController,
    private val context: Context
) {
    open fun openThread(data: UrlDataResult.Thread) {
        navController.navigate(
            R.id.action_global_threadFragment,
            Bundle().apply {
                threadType = data.threadType.value
                threadId = data.threadId
                targetPostId = if (data.threadType == ThreadType.THREAD_TYPE_POST_WITH_COMMENTS) TARGET_POSITION_FIRST else TARGET_POSITION_FIRST
            }
        )
    }

    open fun openMessageInThread(data: UrlDataResult.MessageInThread) {
        navController.navigate(
            R.id.action_global_threadFragment,
            Bundle().apply {
                threadType = data.threadType.value
                threadId = data.threadId
                targetPostId = data.postId
            }
        )
    }

    open fun openUserAccount(data: UrlDataResult.UserAccount) {
        navController.navigate(
            R.id.action_global_userProfileFragment,
            Bundle().apply {
                userId = data.userId
            }
        )
    }

    open fun openUnknown(url: String?) {
        try {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse(url)
            context.startActivity(openURL)
        } catch (e: Exception) {
            println(e.message.toString())
        }
    }

}

enum class UrlType(val value: Byte) {
    URL_TYPE_UNKNOWN(0),
    URL_TYPE_THREAD(1),
    URL_TYPE_MESSAGE_IN_THREAD(2),
    URL_TYPE_USER_ACCOUNT(3);
}

val urlTypeSerializer = object : TypeAdapter<UrlType>() {
    override fun write(out: JsonWriter, value: UrlType?) {
        out.value(value?.value)
    }

    override fun read(`in`: JsonReader): UrlType? =
        `in`.nextInt()
            .toByte()
            .let { serialized ->
                UrlType.values().find {
                    it.value == serialized
                }
            }
}

data class UrlDataResultDto(
    @SerializedName("type")
    val type: UrlType,
    @SerializedName("thread_type")
    val threadType: ThreadType? = null,
    @SerializedName("thread_id")
    val threadId: Long? = null,
    @SerializedName("post_id")
    val postId: Long? = null,
    @SerializedName("user_id")
    val userId: Long? = null,
)

fun UrlDataResultDto.toDomainModel(): UrlDataResult =
    when (type) {
        UrlType.URL_TYPE_THREAD -> UrlDataResult.Thread(
            threadType = requireNotNull(threadType),
            threadId = requireNotNull(threadId),
        )
        UrlType.URL_TYPE_MESSAGE_IN_THREAD -> UrlDataResult.MessageInThread(
            threadType = requireNotNull(threadType),
            threadId = requireNotNull(threadId),
            postId = requireNotNull(postId),
        )
        UrlType.URL_TYPE_USER_ACCOUNT -> UrlDataResult.UserAccount(
            userId = requireNotNull(userId),
        )
        UrlType.URL_TYPE_UNKNOWN -> UrlDataResult.Unknown(0)
    }

sealed interface UrlDataResult {
    data class Unknown(
        val type: Byte
    ) : UrlDataResult

    data class Thread(
        val threadType: ThreadType,
        val threadId: Long,
    ) : UrlDataResult

    data class UserAccount(
        val userId: Long
    ) : UrlDataResult

    data class MessageInThread(
        val threadType: ThreadType,
        val threadId: Long,
        val postId: Long,
    ) : UrlDataResult
}