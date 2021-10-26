package club.electro.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import club.electro.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import club.electro.auth.AppAuth
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {
    val ACTION_LIKE = "LIKE"
    val ACTION_POST = "POST"

    private val action = "action"
    private val content = "content"
    private val channelId = "remote"

    private val gson = Gson()

    private val actionNewThreadPost = "newThreadPost"

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

    override fun onMessageReceived(message: RemoteMessage) {
//        println("message invoked " + Gson().toJson(message))
//        println("Action: " + message.data.get("action"))
//
        val action = message.data.get(action)

        when (action) {
            actionNewThreadPost -> {
                val data = gson.fromJson(message.data[content], PostNotification::class.java)
                println("Author: " + data.authorName);

                val notification = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.electro_club_icon)
                    .setContentTitle(
                        getString(
                            R.string.notification_user_posted,
                            data.authorName
                        )
                    )
                    .setContentText(data.postContent)
                    .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(data.postContent))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build()

                NotificationManagerCompat.from(this)
                    .notify(Random.nextInt(100_000), notification)
            }
        }

//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.electro_club_icon)
//            .setContentText(data.authorName)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//        NotificationManagerCompat.from(this)
//            .notify(Random.nextInt(100_000), notification)

//        message.data[action]?.let {
//            when (it) {
//                ACTION_LIKE -> handleLike(gson.fromJson(message.data[content], LikeNotification::class.java))
//                ACTION_POST -> handlePost(gson.fromJson(message.data[content], PostNotification::class.java))
//                else -> println("Unrecognizable notification received")
//            }
//        }

//          handleRecipientData(gson.fromJson(message.data[content], RecipientData::class.java))


//        if (message.data[recipientId] == 0) {
//            AppAuth.getInstance().sendPushToken()
//        }
    }

    override fun onNewToken(token: String) {
        AppAuth.getInstance().sendPushToken(token)
    }

//    private fun handleRecipientData(data: RecipientData) {
//        println("Handled: " + data.recipientId)
//        when (data.recipientId) {
//            null -> {
//                showSimpleMessage(data.content)
//            }
//
//            AppAuth.getInstance().myId() -> {
//                showSimpleMessage(data.content)
//            }
//
//            else -> {
//                AppAuth.getInstance().sendPushToken()
//            }
//        }
//    }
//
//    private fun showSimpleMessage(text: String) {
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentText(text)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//        NotificationManagerCompat.from(this)
//            .notify(Random.nextInt(100_000), notification)
//    }

//    private fun handlePost(content: PostNotification) {
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle(
//                getString(
//                    R.string.notification_user_posted,
//                    content.userName
//                )
//            )
//            .setContentText(content.postContent)
//            .setStyle(NotificationCompat.BigTextStyle()
//                .bigText(content.postContent))
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//
//        NotificationManagerCompat.from(this)
//            .notify(Random.nextInt(100_000), notification)
//    }
//
//    private fun handleLike(content: LikeNotification) {
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle(
//                getString(
//                    R.string.notification_user_liked,
//                    content.userName,
//                    content.postAuthor,
//                )
//            )
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//
//        NotificationManagerCompat.from(this)
//            .notify(Random.nextInt(100_000), notification)
//    }
}



data class PostNotification (
    val authorName: String,
    val postContent: String,
    val threadType: Byte,
    val threadId: Long,
    val postId: Long
)