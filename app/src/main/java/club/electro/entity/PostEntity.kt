package club.electro.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import club.electro.dto.Post

@Entity(indices = [Index(value = ["id"], unique = true)])
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long,
    val id: Long,
    val status: Byte,
    val threadType: Byte,
    val threadId: Long,
    val authorId: Long,
    val authorName: String,
    val authorAvatar: String,
    val content: String,
    val published: Long,
    val likes: Int = 0,
    val views: Int = 0,

    val canEdit: Boolean = false,
    val canRemove: Boolean = false
) {
    fun toDto() = Post(
        id = id,
        status = status,
        threadType = threadType,
        threadId = threadId,
        authorId = authorId,
        authorName = authorName,
        authorAvatar = authorAvatar,
        content = content,
        published = published,
        likes = likes,
        views = views,
        canEdit = canEdit,
        canRemove = canRemove
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                localId = 0,
                id = dto.id,
                status = dto.status,
                threadType = dto.threadType,
                threadId = dto.threadId,
                authorId = dto.authorId,
                authorName = dto.authorName,
                authorAvatar = dto.authorAvatar,
                content = dto.content,
                published = dto.published,
                likes = dto.likes,
                views = dto.views,
                canEdit = dto.canEdit,
                canRemove = dto.canRemove
            )
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)