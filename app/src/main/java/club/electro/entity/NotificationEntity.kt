package club.electro.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val notificationId: Int,
    val threadType: Byte,
    val threadId: Long,
)