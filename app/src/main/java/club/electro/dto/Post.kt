package club.electro.dto

import com.google.gson.annotations.SerializedName

data class Post (
    val localId: Long,
    val id: Long,
    val status: Byte,
    @SerializedName("thread_type")  val threadType: Byte,
    @SerializedName("thread_id") val threadId: Long,
    @SerializedName("author_id") val authorId: Long?,
    @SerializedName("author_name") val authorName: String? = null,
    @SerializedName("author_avatar") val authorAvatar: String? = null,
    val content: String = "",
    val preparedContent: String? = null,
    val published: Long,
    @SerializedName("answer_to") val answerTo: Long? = null,
    val likes: Int = 0,
    val views: Int = 0,

    val canEdit: Boolean = false,
    val canRemove: Boolean = false,

    val attachments: List<PostAttachment>? = null,
) {
    companion object {
        const val STATUS_PUBLISHED: Byte = 0
        const val STATUS_CREATED_LOCAL: Byte = 101
        const val STATUS_SAVING_LOCAL: Byte = 102
        const val STATUS_REMOVING_LOCAL: Byte = 103
        const val STATUS_WAITING_FOR_LOAD: Byte = 104
    }
}

data class PostAttachment(
    val type: Byte,
    val url: String,
)
