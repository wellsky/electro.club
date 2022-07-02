package club.electro.repository.notifications

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import club.electro.dao.NotificationDao
import club.electro.entity.NotificationEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class NotificationsRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    @ApplicationContext private val context: Context
): NotificationsRepository {
    override fun insert(notificationId: Int, threadType: Byte, threadId: Long) {
        notificationDao.insert(
            NotificationEntity(
            notificationId = notificationId,
            threadType = threadType,
            threadId = threadId
        ))
    }

    override fun getByThread(threadType: Byte, threadId: Long): Int? =
        notificationDao.getByThread(threadType, threadId)?.let {
            it.notificationId
        }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun clearByThread(threadType: Byte, threadId: Long) {
        getByThread(threadType, threadId)?.let {
            NotificationManagerCompat.from(context).cancel(it)
        }
        notificationDao.clearByThread(threadType, threadId)

        clearAllIfLast()
    }

    /**
     * Надо удалить последнюю нотификашку, чтобы не оставалась пустая summary notification
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun clearAllIfLast() {
        val activeNotifications =
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .activeNotifications

        if (activeNotifications.size == 1) {
            NotificationManagerCompat.from(context).cancelAll()
        }
    }
}
