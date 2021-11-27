package club.electro.utils

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
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




class UrlHandler(val fragment: Fragment) {
    private val PRIMARY_HOST = "electro.club"
    private val PATH_USERS = "users"

    private val diContainer = DependencyContainer.getInstance()
    private val apiService = diContainer.apiService
    private val resources = diContainer.context.resources

    fun open(url: String?) {

        url?.let { url ->
            val uri = URI(url)
            val host: String = uri.host

            if (host.equals(PRIMARY_HOST)) {
                val path: String = uri.path
                if (path.isNotBlank()) {
                    val firstPath = path.substringAfter("/").substringBefore("/")

                    when (firstPath) {
                        PATH_USERS -> {
                            val urlUserId = path.substringAfter("/").substringAfter("/").toLong()

                            fragment.findNavController().navigate(
                                R.id.action_global_userProfileFragment,
                                Bundle().apply {
                                    userId = urlUserId
                                }
                            )
                            return@open
                        }

                        else -> {
                            CoroutineScope(Dispatchers.Main).launch {
                                val response = apiService.getUrlData(
                                    access_token = resources.getString(R.string.electro_club_access_token),
                                    url = url
                                )
                                val body = response.body() ?: throw ApiError(response.code(), response.message())

                                navigateToThread(body.data.thread_type!!, body.data.thread_id!!)
                            }

                            // val body = response.body() ?: throw ApiError(response.code(), response.message())

//                            fragment.findNavController().navigate(
//                                R.id.action_global_threadFragment,
//                                Bundle().apply {
//                                    threadType = body.data.thread_type!!
//                                    threadId = body.data.thread_id!!
//                                }
//                            )
//                            return@open

                        }
                    }
                }
            }

            //openInBrowser(url)
        }
    }

    fun navigateToThread(type: Byte, id: Long) {
        fragment.findNavController().navigate(
            R.id.action_global_threadFragment,
            Bundle().apply {
                threadType = type
                threadId = id
            }
        )

    }

    fun openInBrowser(url: String?) {
        try {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse(url)
            fragment.context?.startActivity(openURL)
        } catch (e: Exception) {
            println(e.message.toString())
        }
    }
}