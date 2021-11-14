package club.electro.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import club.electro.MainActivity
import club.electro.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import club.electro.auth.AppAuth
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import club.electro.utils.GetCircleBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {
    private val ACTION_THREAD_POST = "newThreadPost"
    private val ACTION_ANSWER = "newAnswer"
    private val ACTION_QUOTE = "newQuote"
    private val ACTION_MENTION = "newMention"
    private val ACTION_PERSONAL_MESSAGE = "newPersonalMessage"

    private val recipientId = "recipientId"
    private val action = "action"
    private val content = "content"
    private val channelId = "remote"

    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onNewToken(token: String) {
        AppAuth.getInstance().sendPushToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        //println("Message invoked " + Gson().toJson(message))

        val recipient = message.data.get(recipientId)
        val currenUserId = AppAuth.getInstance().myId()

        recipient?.let {
            if (it.toLong() == currenUserId) {
                try {
                    handleNewMessage(message)
                } catch (e: Exception) {
                    println("Error: " + e.message.toString())
                }
            } else {
                AppAuth.getInstance().sendPushToken()
            }
        }
    }

    private fun handleNewMessage(message: RemoteMessage) {
        val action = message.data.get(action)

        when (action) {
            ACTION_PERSONAL_MESSAGE, ACTION_THREAD_POST, ACTION_ANSWER, ACTION_MENTION, ACTION_QUOTE -> {
                val data = gson.fromJson(message.data[content], PostNotification::class.java)

                // https://stackoverflow.com/questions/26608627/how-to-open-fragment-page-when-pressed-a-notification-in-android
                val resultPendingIntent = NavDeepLinkBuilder(this)
                    .setComponentName(MainActivity::class.java)
                    .setGraph(R.navigation.mobile_navigation)
                    .setDestination(R.id.threadFragment)
                    .setArguments(Bundle().apply {
                        threadType = data.threadType
                        threadId = data.threadId
                    })
                    .createPendingIntent()

                val groupKey = "THREAD-" + data.threadType + "-" + data.threadId

                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.electro_club_icon_grey_64)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true)
                    .setGroup(groupKey)

                if (action.equals(ACTION_PERSONAL_MESSAGE)) {
                    notificationBuilder
                        .setContentTitle(
                            getString(
                                R.string.notification_personal_message,
                                data.authorName
                            )
                        )
                        .setContentText(data.postContent)
                        .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(data.postContent))
                }

                if (action.equals(ACTION_THREAD_POST)) {
                    notificationBuilder
                        .setContentTitle(data.threadName)
                        .setContentText(data.authorName + ": " + data.postContent)
                        .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(data.authorName + ": " + data.postContent))

                }

                applyImageUrl(notificationBuilder, data.threadImage)
                val notification = notificationBuilder.build()
                NotificationManagerCompat.from(this)
                    .notify(Random.nextInt(100_000), notification)
            }
        }
    }

    fun applyImageUrl(
        builder: NotificationCompat.Builder,
        imageUrl: String
    ) = runBlocking {
        val url = URL(imageUrl)

        withContext(Dispatchers.IO) {
            try {
                val input = url.openStream()
                BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                null
            }
        }?.let { bitmap ->
            val roundedBitmap = GetCircleBitmap.make(bitmap)
            builder.setLargeIcon(roundedBitmap)
        }
    }
}

data class PostNotification (
    val recipientId: Long,
    val threadName: String,
    val threadImage: String,
    val authorName: String,
    val postContent: String,
    val threadType: Byte,
    val threadId: Long,
    val postId: Long
)