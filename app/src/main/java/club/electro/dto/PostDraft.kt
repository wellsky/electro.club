package club.electro.dto

data class PostDraft (
    val id: Long,
    val threadType: Byte,
    val threadId: Byte,
    val content: String,
    val answerTo: Long,
)