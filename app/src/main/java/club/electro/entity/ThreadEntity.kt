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
    val subscribersCount: Long,
) {
    fun toDto() = PostsThread(
        id = id,
        type = type,
        name = name,
        image = image,
        subscribersCount = subscribersCount
    )

    companion object {
        fun fromDto(dto: PostsThread) =
            ThreadEntity(
                id = dto.id,
                type = dto.type,
                name = dto.name,
                image = dto.image,
                subscribersCount = dto.subscribersCount
            )
    }
}

fun List<ThreadEntity>.toDto(): List<PostsThread> = map(ThreadEntity::toDto)
fun List<PostsThread>.toEntity(): List<ThreadEntity> = map(ThreadEntity::fromDto)
fun PostsThread.toEntity(): ThreadEntity = ThreadEntity.fromDto(this)