package club.electro.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import club.electro.entity.NotificationEntity

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(notification: NotificationEntity)

    @Query("SELECT * FROM NotificationEntity WHERE threadType = :threadType AND threadId = :threadId")
    fun getByThread(threadType: Byte, threadId: Long): NotificationEntity?

    @Query("DELETE FROM NotificationEntity WHERE threadType = :threadType AND threadId = :threadId")
    fun clearByThread(threadType: Byte, threadId: Long)

    @Query("DELETE FROM NotificationEntity")
    fun clearAllConversations()
}