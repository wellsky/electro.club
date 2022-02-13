package club.electro.dto

data class PostDraftAttachment (
    val id: Long,
    val type: Byte,
    val threadType: Byte,
    val threadId: Long,
    val localFile: String,
    val previewUrl: String?,
    val fullUrl: String?,
    val status: Byte,
)