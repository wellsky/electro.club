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
import club.electro.ui.thread.ThreadFragment.Companion.postId
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import club.electro.ui.user.UserProfileFragment.Companion.userId
import com.google.gson.annotations.SerializedName
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI

@AssistedFactory
interface UrlHandlerFactory {
    fun create(navController: NavController): UrlHandler
}

class UrlHandler @AssistedInject constructor(
    @Assisted val navController: NavController,
    @ApplicationContext val context: Context,
    val apiService: ApiService
) {
    private val PRIMARY_HOST = "electro.club"
    private val PATH_USERS = "users"
    private var url: String? = ""

    fun setUrl(url: String?): UrlHandler {
        this.url = url
        return this
    }

    fun open() {
        url?.let { url ->
            val uri = URI(url)
            val host: String = uri.host

            if (host == PRIMARY_HOST) {
                parseUriLocal(uri)
                    ?.let(UrlDataResultDto::toDomainModel)
                    ?.let(this::openUrlData)
                    ?: parseUriRemoteAndOpen(uri)
            } else {
                openInBrowser(uri.toString())
            }
        }
    }

    fun parseUriLocal(uri: URI): UrlDataResultDto? {
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


    fun parseUriRemoteAndOpen(uri: URI) {
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

    fun openUrlData(data: UrlDataResult) {
        when (data) {
            is UrlDataResult.Thread -> {
                navController.navigate(
                    R.id.action_global_threadFragment,
                    Bundle().apply {
                        threadType = data.threadType.value
                        threadId = data.threadId
                        postId = if (data.threadType == ThreadType.THREAD_TYPE_POST_WITH_COMMENTS) TARGET_POSITION_FIRST else TARGET_POSITION_FIRST
                    }
                )
            }
            is UrlDataResult.UserAccount -> {
                navController.navigate(
                    R.id.action_global_userProfileFragment,
                    Bundle().apply {
                        userId = data.userId
                    }
                )
            }
            is UrlDataResult.MessageInThread -> {
                navController.navigate(
                    R.id.action_global_threadFragment,
                    Bundle().apply {
                        threadType = data.threadType.value
                        threadId = data.threadId
                        postId = data.postId
                    }
                )
            }
        }
    }


    fun openInBrowser(url: String?) {
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
    URL_TYPE_THREAD(1),
    URL_TYPE_MESSAGE_IN_THREAD(2),
    URL_TYPE_USER_ACCOUNT(3);
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
    }

sealed interface UrlDataResult {
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