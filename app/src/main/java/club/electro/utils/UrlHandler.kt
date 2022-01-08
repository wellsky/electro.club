package club.electro.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import club.electro.R
import club.electro.di.DependencyContainer
import club.electro.dto.THREAD_TYPE_POST_WITH_COMMENTS
import club.electro.error.ApiError
import club.electro.repository.ThreadLoadTarget.Companion.TARGET_POSITION_FIRST
import club.electro.ui.thread.ThreadFragment.Companion.postId
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import club.electro.ui.user.UserProfileFragment.Companion.userId
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.net.URI




class UrlHandler(val context: Context, val navController: NavController) {
    companion object {
        private val PRIMARY_HOST = "electro.club"
        private val PATH_USERS = "users"

        private val URL_TYPE_THREAD: Byte = 1
        private val URL_TYPE_MESSAGE_IN_THREAD: Byte = 2
        private val URL_TYPE_USER_ACCOUNT: Byte = 3
    }

    private val diContainer = DependencyContainer.getInstance()
    private val apiService = diContainer.apiService

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
                parseUriLocal(uri)?.let {
                    openUrlData(it)
                } ?: parseUriRemoteAndOpen(uri)
            } else {
                openInBrowser(uri.toString())
            }
        }
    }

    fun parseUriLocal(uri: URI): UrlDataResult? {
        val path: String = uri.path
        if (path.isNotBlank()) {
            val firstPath = path.substringAfter("/").substringBefore("/")

            when (firstPath) {
                PATH_USERS -> {
                    val urlUserId = path.substringAfter("/").substringAfter("/").toLong()

                    return UrlDataResult(
                        type = URL_TYPE_USER_ACCOUNT,
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

            val result = response.body() ?: throw ApiError(response.code(), response.message())

            openUrlData(result.data)
        }
    }

    fun openUrlData(data: UrlDataResult) {
        when (data.type) {
            URL_TYPE_USER_ACCOUNT -> {
                navController.navigate(
                    R.id.action_global_userProfileFragment,
                    Bundle().apply {
                        userId = data.userId!!
                    }
                )
            }

            URL_TYPE_THREAD -> {
                navController.navigate(
                    R.id.action_global_threadFragment,
                    Bundle().apply {
                        threadType = data.threadType!!
                        threadId = data.threadId!!
                        postId = if (data.threadType == THREAD_TYPE_POST_WITH_COMMENTS) TARGET_POSITION_FIRST else TARGET_POSITION_FIRST
                    }
                )
            }

            URL_TYPE_MESSAGE_IN_THREAD -> {
                navController.navigate(
                    R.id.action_global_threadFragment,
                    Bundle().apply {
                        threadType = data.threadType!!
                        threadId = data.threadId!!
                        postId = data.postId!!
                    }
                )
            }

            else -> {
                openInBrowser(url)
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

data class UrlDataResult(
    @SerializedName("type")
    val type: Byte,
    @SerializedName("thread_type")
    val threadType: Byte? = null,
    @SerializedName("thread_id")
    val threadId: Long? = null,
    @SerializedName("post_id")
    val postId: Long? = null,
    @SerializedName("user_id")
    val userId: Long? = null,
)