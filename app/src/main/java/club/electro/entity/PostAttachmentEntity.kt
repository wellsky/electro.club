package club.electro.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey
import club.electro.dto.PostAttachment

@Entity
data class PostAttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: Byte,
    val threadType: Byte,
    val threadId: Long,
    val localFile: String,
    val previewUrl: String? = null,
    val fullUrl: String? = null,
    val status: Byte = 0,
) {
    fun toDto() = PostAttachment(
        id = id,
        type = type,
        threadType = threadType,
        threadId = threadId,
        localFile = localFile,
        previewUrl = previewUrl,
        fullUrl = fullUrl,
        status = status
    )

    companion object {
        fun fromDto(dto: PostAttachment) =
            PostAttachmentEntity(
                id = dto.id,
                type = dto.type,
                threadType = dto.threadType,
                threadId = dto.threadId,
                localFile = dto.localFile,
                previewUrl = dto.previewUrl,
                fullUrl = dto.fullUrl,
                status = dto.status
            )
    }
}

fun List<PostAttachmentEntity>.toDto(): List<PostAttachment> = map(PostAttachmentEntity::toDto)
fun List<PostAttachment>.toEntity(): List<PostAttachmentEntity> = map(PostAttachmentEntity::fromDto)
fun PostAttachment.toEntity(): PostAttachmentEntity = PostAttachmentEntity.fromDto(this)
