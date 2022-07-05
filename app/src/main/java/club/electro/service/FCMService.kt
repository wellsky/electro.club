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
import androidx.core.graphics.drawable.IconCompat
import androidx.navigation.NavDeepLinkBuilder
import club.electro.MainActivity
import club.electro.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import club.electro.auth.AppAuth
import club.electro.dto.ThreadType
import club.electro.repository.notifications.NotificationsRepository
import club.electro.ui.thread.ThreadFragment.Companion.targetPostId
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import club.electro.utils.GetCircleBitmap
import club.electro.utils.loadCircleImageBlocking
import club.electro.utils.toPlainText
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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

    private val groupKey = "electro_club" // Нотификашки группируются все в одну по этому ключу
    private val groupTitle = "electro.club" // Заголовок суммарной нотификашки

    private val gson = Gson()

    @Inject
    lateinit var appAuth: AppAuth

    @Inject
    lateinit var notificationsRepository: NotificationsRepository

    @Inject
    @ApplicationContext
    lateinit var context: Context

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
        runBlocking {
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
    }

    private fun findActiveNotification(notificationId: Int): Notification? {
        return (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .activeNotifications.find { it.id == notificationId }?.notification
    }

    // https://medium.com/@sidorovroman3/android-how-to-use-messagingstyle-for-notifications-without-caching-messages-c414ef2b816c
    private fun addReplyToNotification(
        notificationId: Int,
        postNotification: PostNotification,
    ) {
        val text = postNotification.postContent.toPlainText()

        // Find notification that you want to update.
        val activeNotification = findActiveNotification(notificationId) ?: return

        // Extract MessagingStyle object from the active notification.
        val activeStyle = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(activeNotification)

        // Recover builder from the active notification.
        val recoveredBuilder = NotificationCompat.Builder(context, activeNotification)

        val you: Person = Person
            .Builder()
            .setName("You")
            .build()

        // The recoveredBuilder is Notification.Builder whereas the activeStyle is NotificationCompat.MessagingStyle.
        // It means you need to recreate the style as Notification.MessagingStyle to make it compatible with the builder.
        val newStyle = NotificationCompat.MessagingStyle(you)

        newStyle.conversationTitle = activeStyle?.conversationTitle
        newStyle.isGroupConversation = activeStyle?.isGroupConversation ?: false

        activeStyle?.messages?.forEach {
            newStyle.addMessage(NotificationCompat.MessagingStyle.Message(it.text, it.timestamp * 1000, it.person))
        }

        //val authorIcon = loadUrl(postNotification.authorName)

        val author: Person = Person
            .Builder()
            .setName(postNotification.authorName)
            .setIcon(IconCompat.createWithBitmap(loadCircleImageBlocking(postNotification.authorImage)))
            .build()

        // Add your reply to the new style.
        newStyle.addMessage(NotificationCompat.MessagingStyle.Message(text, System.currentTimeMillis(), author))

        // Set the new style to the recovered builder.
        recoveredBuilder.setStyle(newStyle)


        recoveredBuilder.setWhen(postNotification.published * 1000)
        recoveredBuilder.setShowWhen(true)
        // TODO не понятно надо это или нет. Иконки группы не отображаются.
        // applyImageUrl(recoveredBuilder, postNotification.threadImage)

        // Update the active notification.
        NotificationManagerCompat.from(context).notify(notificationId, recoveredBuilder.build())
    }

    private fun NotificationCompat.Builder.createConversation(
        postNotification: PostNotification
    ): NotificationCompat.Builder {
        // https://stackoverflow.com/questions/26608627/how-to-open-fragment-page-when-pressed-a-notification-in-android
        val resultPendingIntent = NavDeepLinkBuilder(context)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(R.id.threadFragment)
            .setArguments(Bundle().apply {
                threadType = postNotification.threadType
                threadId = postNotification.threadId
                targetPostId = postNotification.postId
            })
            .createPendingIntent()

        this.setContentIntent(resultPendingIntent)

        this.setWhen(postNotification.published * 1000)
        this.setShowWhen(true)

        val you: Person = Person
            .Builder()
            .setName("You")
            .build()

        val author: Person = Person
            .Builder()
            .setName(postNotification.authorName)
            .setIcon(IconCompat.createWithBitmap(loadCircleImageBlocking(postNotification.authorImage)))
            .build()

        val style = NotificationCompat.MessagingStyle(you)
            .setConversationTitle(postNotification.threadName)
            .addMessage(postNotification.postContent.toPlainText(), postNotification.published * 1000, author)

        if (postNotification.threadType != ThreadType.THREAD_TYPE_PERSONAL_CHAT.value) {
            style.isGroupConversation = true
        }

        this.setStyle(style)

        //applyImageUrl(this, data.threadImage)

        return this
    }

    private fun getExistingConversation(
        threadType: Byte,
        threadId: Long,
    ): Int? {
        val existsId = notificationsRepository.getByThread(threadType, threadId)

        if (existsId != null) {
            if (findActiveNotification(existsId) != null) {
                return existsId
            }
        }
        return null
    }

    private fun defaultNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, channelIdKey)
            .setSmallIcon(R.drawable.electro_club_icon_grey_64)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(groupKey)
    }

    private fun handleNewMessage(message: RemoteMessage) {
        val action = message.data[actionKey]

        when (action) {
            ACTION_PERSONAL_MESSAGE, ACTION_THREAD_POST, ACTION_ANSWER, ACTION_MENTION, ACTION_QUOTE -> {
                val postNotification = gson.fromJson(message.data[contentKey], PostNotification::class.java)

                val notificationBuilder = defaultNotificationBuilder()

                getExistingConversation(postNotification.threadType, postNotification.threadId)?.let {
                    addReplyToNotification(
                        notificationId = it,
                        postNotification = postNotification,
                    )

                    addGroupNotification()
                } ?: run{
                    notificationsRepository.clearByThread(postNotification.threadType, postNotification.threadId)

                    notificationBuilder.createConversation(postNotification)

                    val notification = notificationBuilder.build()

                    val notificationId = Random.nextInt(100_000)

                    NotificationManagerCompat.from(this)
                        .notify(notificationId, notification)

                    notificationsRepository.insert(
                        notificationId = notificationId,
                        threadType = postNotification.threadType,
                        threadId = postNotification.threadId
                    )

                    addGroupNotification()
                }
            }
        }
    }

    private fun addGroupNotification() {
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
            .notify(notificationsRepository.groupNotificationId, summaryNotification)
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

data class PostNotification(
    val recipientId: Long,
    val threadName: String,
    val threadImage: String,
    val authorName: String,
    val authorImage: String,
    val postContent: String,
    val published: Long,
    val threadType: Byte,
    val threadId: Long,
    val postId: Long
)
