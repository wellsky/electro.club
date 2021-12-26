package club.electro.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import club.electro.dto.PostsThread
import club.electro.dto.User

@Entity
data class ThreadEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val type: Byte,
    val name: String,
    val image: String,
    val messages: Long,
    val subscribersCount: Long,
    val subscriptionStatus: Byte,
) {
    fun toDto() = PostsThread(
        id = id,
        type = type,
        name = name,
        image = image,
        messages = messages,
        subscribersCount = subscribersCount,
        subscriptionStatus = subscriptionStatus
    )

    companion object {
        fun fromDto(dto: PostsThread) =
            ThreadEntity(
                id = dto.id,
                type = dto.type,
                name = dto.name,
                image = dto.image,
                messages = dto.messages,
                subscribersCount = dto.subscribersCount,
                subscriptionStatus = dto.subscriptionStatus
            )
    }
}

fun List<ThreadEntity>.toDto(): List<PostsThread> = map(ThreadEntity::toDto)
fun List<PostsThread>.toEntity(): List<ThreadEntity> = map(ThreadEntity::fromDto)
fun PostsThread.toEntity(): ThreadEntity = ThreadEntity.fromDto(this)