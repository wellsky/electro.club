package club.electro.repository.notifications

import club.electro.entity.NotificationEntity

interface NotificationsRepository {
    fun insert(notificationId: Int, threadType: Byte, threadId: Long)
    fun getByThread(threadType: Byte, threadId: Long): Int?
    fun clearByThread(threadType: Byte, threadId: Long)
}