package club.electro.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NotificationMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val notificationId: Long,
    val threadType: Byte,
    val threadId: Long,
    val authorName: String?,
    val authorImage: String?,
    val text: String?
)