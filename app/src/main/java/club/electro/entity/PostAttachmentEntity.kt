package club.electro.entity;

import androidx.room.Entity;
import androidx.room.Index
import androidx.room.PrimaryKey
import club.electro.dto.PostAttachment

@Entity(indices = [Index(value = ["threadType", "threadId", "id"], unique = true)])
data class PostAttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val id: Long? = null,
    val threadType: Byte,
    val threadId: Long,
    val type: Byte,
    val name: String? = null,
    val localPath: String? = null,
    val previewUrl: String? = null,
    val fullUrl: String? = null,
    val status: Byte = 0,
) {
    fun toDto() = PostAttachment(
        localId = localId,
        id = id,
        type = type,
        threadType = threadType,
        threadId = threadId,
        name = name,
        localPath = localPath,
        previewUrl = previewUrl,
        fullUrl = fullUrl,
        status = status
    )

    companion object {
        fun fromDto(dto: PostAttachment) =
            PostAttachmentEntity(
                localId = dto.localId,
                id = dto.id,
                type = dto.type,
                threadType = dto.threadType,
                threadId = dto.threadId,
                name = dto.name,
                localPath = dto.localPath,
                previewUrl = dto.previewUrl,
                fullUrl = dto.fullUrl,
                status = dto.status
            )
    }
}

fun List<PostAttachmentEntity>.toDto(): List<PostAttachment> = map(PostAttachmentEntity::toDto)
fun List<PostAttachment>.toEntity(): List<PostAttachmentEntity> = map(PostAttachmentEntity::fromDto)
fun PostAttachment.toEntity(): PostAttachmentEntity = PostAttachmentEntity.fromDto(this)
