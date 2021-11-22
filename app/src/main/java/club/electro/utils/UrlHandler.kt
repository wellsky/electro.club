package club.electro.utils

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import club.electro.R
import club.electro.ui.user.UserProfileFragment.Companion.userId
import java.net.URI




class UrlHandler(val fragment: Fragment) {
    private val PATH_USERS = "users"

    fun open(url: String?) {

        url?.let { url ->
            val uri = URI(url)
            val path: String = uri.getPath()
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
                }
            }
            openInBrowser(url)
        }
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