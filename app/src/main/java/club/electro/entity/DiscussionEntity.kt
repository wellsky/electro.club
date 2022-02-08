package club.electro.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import club.electro.dto.Discussion

@Entity(indices = [Index(value = ["threadType", "threadId"], unique = true)])
data class DiscussionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val transportId: Long,
    val seriesId: Long,
    val threadType: Byte,
    val threadId: Long,
    val title: String,
    val image: String,
    val messages: Long,
    val lastMessageTime: Long,
) {
    fun toDto() = Discussion(
        transportId = transportId,
        seriesId = seriesId,
        threadType = threadType,
        threadId = threadId,
        title = title,
        image = image,
        messages = messages,
        lastMessageTime = lastMessageTime
    )

    companion object {
        fun fromDto(dto: Discussion) =
            DiscussionEntity(
                transportId = dto.transportId,
                seriesId = dto.seriesId,
                threadType = dto.threadType,
                threadId = dto.threadId,
                title = dto.title,
                image = dto.image,
                messages = dto.messages,
                lastMessageTime = dto.lastMessageTime
            )
    }
}

fun List<DiscussionEntity>.toDto(): List<Discussion> = map(DiscussionEntity::toDto)
fun List<Discussion>.toEntity(): List<DiscussionEntity> = map(DiscussionEntity::fromDto)
