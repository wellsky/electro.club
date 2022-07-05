package club.electro.repository.notifications

interface NotificationsRepository {
    val groupNotificationId: Int

    fun insert(notificationId: Int, threadType: Byte, threadId: Long)
    fun getByThread(threadType: Byte, threadId: Long): Int?
    fun clearByThread(threadType: Byte, threadId: Long)
    suspend fun clearAllConversations()
}