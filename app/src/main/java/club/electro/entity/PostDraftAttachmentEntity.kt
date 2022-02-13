package club.electro.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey
import club.electro.dto.PostDraftAttachment

@Entity
data class PostDraftAttachmentEntity(
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
    fun toDto() = PostDraftAttachment(
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
        fun fromDto(dto: PostDraftAttachment) =
            PostDraftAttachmentEntity(
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

fun List<PostDraftAttachmentEntity>.toDto(): List<PostDraftAttachment> = map(PostDraftAttachmentEntity::toDto)
fun List<PostDraftAttachment>.toEntity(): List<PostDraftAttachmentEntity> = map(PostDraftAttachmentEntity::fromDto)
fun PostDraftAttachment.toEntity(): PostDraftAttachmentEntity = PostDraftAttachmentEntity.fromDto(this)
