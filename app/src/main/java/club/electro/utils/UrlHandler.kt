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
import club.electro.error.ApiError
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import club.electro.ui.user.UserProfileFragment.Companion.userId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.net.URI




class UrlHandler(val context: Context, val navController: NavController) {
    private val PRIMARY_HOST = "electro.club"
    private val PATH_USERS = "users"

    private val URL_TYPE_THREAD: Byte = 1
    private val URL_TYPE_MESSAGE_IN_THREAD: Byte  = 2
    private val URL_TYPE_USER_ACCOUNT: Byte  = 3

    private val diContainer = DependencyContainer.getInstance()
    private val apiService = diContainer.apiService
    private val resources = diContainer.context.resources

    private var url: String? = ""

    fun setUrl(url: String?): UrlHandler {
        this.url = url
        return this
    }

    fun open() {
        url?.let { url ->
            val uri = URI(url)
            val host: String = uri.host

            if (host.equals(PRIMARY_HOST)) {
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
                        user_id = urlUserId
                    )
                }
            }
        }
        return null
    }


    fun parseUriRemoteAndOpen(uri: URI) {
        CoroutineScope(Dispatchers.Main).launch {
            val response = apiService.getUrlData(
                access_token = resources.getString(R.string.electro_club_access_token),
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
                        userId = data.user_id!!
                    }
                )
            }

            URL_TYPE_THREAD -> {
                navController.navigate(
                    R.id.action_global_threadFragment,
                    Bundle().apply {
                        threadType = data.thread_type!!
                        threadId = data.thread_id!!
                    }
                )
            }

            URL_TYPE_MESSAGE_IN_THREAD -> {
                navController.navigate(
                    R.id.action_global_threadFragment,
                    Bundle().apply {
                        threadType = data.thread_type!!
                        threadId = data.thread_id!!
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


data class UrlDataResult (
    val type: Byte,
    val thread_type: Byte? = null,
    val thread_id: Long? = null,
    val post_id: Long? = null,
    val user_id: Long? = null,
)