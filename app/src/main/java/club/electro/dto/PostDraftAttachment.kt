package club.electro.dto

data class PostDraftAttachment (
    val id: Long,
    val threadType: Byte,
    val threadId: Byte,
    val localFile: String,
    val previewUrl: String,
    val fullUrl: String,
    val status: Byte,
)