package club.electro.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import club.electro.dto.Post
import club.electro.dto.PostAttachment

@Entity(indices = [Index(value = ["threadType", "threadId", "id"], unique = true)])
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long,
    val id: Long,
    val status: Byte,
    val threadType: Byte,
    val threadId: Long,
    val authorId: Long,
    val authorName: String,
    val authorAvatar: String?,
    val content: String,
    val preparedContent: String? = content,
    val published: Long,
    val answerTo: Long?,
    val likes: Int = 0,
    val views: Int = 0,

    val canEdit: Boolean = false,
    val canRemove: Boolean = false,

    val attachments: List<PostAttachment>? = null,

    val fresh: Boolean = false,
) {
    fun toDto() = Post(
        localId = localId,
        id = id,
        status = status,
        threadType = threadType,
        threadId = threadId,
        authorId = authorId,
        authorName = authorName,
        authorAvatar = authorAvatar,
        content = content,
        preparedContent = preparedContent,
        published = published,
        answerTo = answerTo,
        likes = likes,
        views = views,
        canEdit = canEdit,
        canRemove = canRemove,
        attachments = attachments
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                localId = dto.localId,
                id = dto.id,
                status = dto.status,
                threadType = dto.threadType,
                threadId = dto.threadId,
                authorId = dto.authorId,
                authorName = dto.authorName,
                authorAvatar = dto.authorAvatar,
                content = dto.content,
                preparedContent = dto.preparedContent,
                published = dto.published,
                answerTo = dto.answerTo,
                likes = dto.likes,
                views = dto.views,
                canEdit = dto.canEdit,
                canRemove = dto.canRemove,
                attachments = dto.attachments
            )
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)
fun Post.toEntity(): PostEntity = PostEntity.fromDto(this)

