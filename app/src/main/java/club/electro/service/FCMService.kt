package club.electro.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.navigation.NavDeepLinkBuilder
import club.electro.MainActivity
import club.electro.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import club.electro.auth.AppAuth
import club.electro.dao.NotificationDao
import club.electro.entity.NotificationEntity
import club.electro.repository.notifications.NotificationsRepository
import club.electro.ui.thread.ThreadFragment.Companion.targetPostId
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import club.electro.utils.GetCircleBitmap
import club.electro.utils.toPlainText
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {
    companion object {
        private const val ACTION_THREAD_POST = "newThreadPost"
        private const val ACTION_ANSWER = "newAnswer"
        private const val ACTION_QUOTE = "newQuote"
        private const val ACTION_MENTION = "newMention"
        private const val ACTION_PERSONAL_MESSAGE = "newPersonalMessage"
    }

    private val recipientIdKey = "recipientId"
    private val actionKey = "action"
    private val contentKey = "content"
    private val channelIdKey = "remote"

    private val gson = Gson()

    @Inject
    lateinit var appAuth: AppAuth

    @Inject
    lateinit var notificationsRepository: NotificationsRepository

    @Inject
    @ApplicationContext lateinit var context: Context

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelIdKey, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onNewToken(token: String) {
        appAuth.sendPushToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val recipient = message.data[recipientIdKey]
        val currentUserId = appAuth.myId()

        recipient?.let {
            if (it.toLong() == currentUserId) {
                try {
                    handleNewMessage(message)
                } catch (e: Exception) {
                    println("Error: " + e.message.toString())
                }
            } else {
                appAuth.sendPushToken()
            }
        }
    }

    private fun findActiveNotification(notificationId: Int): Notification? {
        return (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .activeNotifications.find { it.id == notificationId }?.notification
    }

    // https://medium.com/@sidorovroman3/android-how-to-use-messagingstyle-for-notifications-without-caching-messages-c414ef2b816c
    private fun addReply(message: CharSequence, notificationId: Int) {
        // Find notification that you want to update.
        val activeNotification = findActiveNotification(notificationId) ?: return

        // Extract MessagingStyle object from the active notification.
        val activeStyle = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(activeNotification)

        // Recover builder from the active notification.
        val recoveredBuilder = Notification.Builder.recoverBuilder(context, activeNotification)

        // The recoveredBuilder is Notification.Builder whereas the activeStyle is NotificationCompat.MessagingStyle.
        // It means you need to recreate the style as Notification.MessagingStyle to make it compatible with the builder.
        val newStyle = Notification.MessagingStyle("you")
        newStyle.conversationTitle = activeStyle?.conversationTitle
        activeStyle?.messages?.forEach {
            newStyle.addMessage(Notification.MessagingStyle.Message(it.text, it.timestamp, it.sender))
        }
        // Add your reply to the new style.
        newStyle.addMessage(Notification.MessagingStyle.Message(message, System.currentTimeMillis(), "you"))

        // Set the new style to the recovered builder.
        recoveredBuilder.setStyle(newStyle)

        // Update the active notification.
        NotificationManagerCompat.from(context).notify(notificationId, recoveredBuilder.build())
    }

    private fun createMessagingStyle(message: RemoteMessage): NotificationCompat.MessagingStyle {
        val data = gson.fromJson(message.data[contentKey], PostNotification::class.java)

        val person: Person = Person.Builder().setName(data.authorName).build()

        return NotificationCompat.MessagingStyle(person)
            .setConversationTitle("Team lunch")
            .addMessage("Hi", data.published, person)
    }

    private fun handleNewMessage(message: RemoteMessage) {
        val action = message.data[actionKey]

        when (action) {
            ACTION_PERSONAL_MESSAGE, ACTION_THREAD_POST, ACTION_ANSWER, ACTION_MENTION, ACTION_QUOTE -> {
                val data = gson.fromJson(message.data[contentKey], PostNotification::class.java)
                val groupTitle = "new messages"
                val groupKey = "electro.club" // Нотификашки группируются все в одну по этому ключу

                // https://stackoverflow.com/questions/26608627/how-to-open-fragment-page-when-pressed-a-notification-in-android
                val resultPendingIntent = NavDeepLinkBuilder(this)
                    .setComponentName(MainActivity::class.java)
                    .setGraph(R.navigation.mobile_navigation)
                    .setDestination(R.id.threadFragment)
                    .setArguments(Bundle().apply {
                        threadType = data.threadType
                        threadId = data.threadId
                        targetPostId = data.postId
                    })
                    .createPendingIntent()

                val notificationBuilder = NotificationCompat.Builder(this, channelIdKey)
                    .setSmallIcon(R.drawable.electro_club_icon_grey_64)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(resultPendingIntent)
                    .setGroup(groupKey)

                if (action == ACTION_PERSONAL_MESSAGE) {
                    val existsId = notificationsRepository.getByThread(data.threadType, data.threadId)
                    println("EXISTS")
                    println(existsId)
                    if (existsId != null) {
                        if (findActiveNotification(existsId) != null) {
                            addReply(data.postContent, existsId)
                            return
                        } else {
                            notificationsRepository.clearByThread(data.threadType, data.threadId)
                        }
                    }

                    notificationBuilder
                        .setStyle(createMessagingStyle(message))
                        .build()
                }

                if (action == ACTION_THREAD_POST) {
                    val text = data.authorName + ": " + data.postContent.toPlainText()
                    notificationBuilder
                        .setContentTitle(data.threadName.toPlainText())
                        .setContentText(text)
                        .setStyle(NotificationCompat.BigTextStyle()
                            .bigText(text)
                        )
                }

                if (action == ACTION_ANSWER) {
                    val text = getString(R.string.notification_answer, data.authorName) + ": " + data.postContent.toPlainText()
                    notificationBuilder
                        .setContentTitle(data.threadName.toPlainText())
                        .setContentText(text)
                        .setStyle(NotificationCompat.BigTextStyle()
                            .bigText(text)
                        )
                }

                if (action == ACTION_QUOTE) {
                    val text = getString(R.string.notification_quoted, data.authorName) + ": " + data.postContent.toPlainText()
                    notificationBuilder
                        .setContentTitle(data.threadName.toPlainText())
                        .setContentText(text)
                        .setStyle(NotificationCompat.BigTextStyle()
                            .bigText(text)
                        )
                }

                if (action == ACTION_MENTION) {
                    val text = getString(R.string.notification_mention, data.authorName) + ": " + data.postContent.toPlainText()
                    notificationBuilder
                        .setContentTitle(data.threadName.toPlainText())
                        .setContentText(text)
                        .setStyle(NotificationCompat.BigTextStyle()
                            .bigText(text)
                        )
                }




                applyImageUrl(notificationBuilder, data.threadImage)
                val notification = notificationBuilder.build()

                val notificationId = Random.nextInt(100_000)

                NotificationManagerCompat.from(this)
                    .notify(notificationId, notification)

                notificationsRepository.insert(
                    notificationId = notificationId,
                    threadType = data.threadType,
                    threadId = data.threadId
                )

                // Нотификашка, которая помещает в себя все остальные нотификашки по ключу groupKey
                val summaryNotification = NotificationCompat.Builder(this, channelIdKey)
                    .setSilent(true)
                    .setContentText(groupTitle)
                    .setSubText(groupTitle)
                    .setSmallIcon(R.drawable.electro_club_icon_grey_64)
                    .setGroup(groupKey)
                    .setGroupSummary(true)
                    .build()

                NotificationManagerCompat.from(this)
                    .notify(1, summaryNotification)
            }
        }
    }

    private fun applyImageUrl(
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
    val published: Long,
    val threadType: Byte,
    val threadId: Long,
    val postId: Long
)