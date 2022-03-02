package club.electro.dto

import com.google.gson.annotations.SerializedName

data class Post (
    val localId: Long,
    @SerializedName("id") val id: Long,
    @SerializedName("status") val status: Byte,
    @SerializedName("thread_type") val threadType: Byte,
    @SerializedName("thread_id") val threadId: Long,
    @SerializedName("author_id") val authorId: Long?,
    @SerializedName("author_name") val authorName: String? = null,
    @SerializedName("author_avatar") val authorAvatar: String? = null,
    @SerializedName("content") val content: String = "",
    @SerializedName("published") val published: Long,
    @SerializedName("answer_to") val answerTo: Long? = null,
    @SerializedName("views") val views: Int? = null,
    @SerializedName("comments") val comments: Int? = null,
    @SerializedName("can_edit") val canEdit: Boolean = false,
    @SerializedName("can_remove") val canRemove: Boolean = false,
    @SerializedName("attachments") val attachmentLinks: List<PostAttachmentLink>? = null,

    val preparedContent: String? = null,
) {
    companion object {
        const val STATUS_PUBLISHED: Byte = 0
        const val STATUS_CREATED_LOCAL: Byte = 101
        const val STATUS_SAVING_LOCAL: Byte = 102
        const val STATUS_REMOVING_LOCAL: Byte = 103
        const val STATUS_WAITING_FOR_LOAD: Byte = 104
    }
}

data class PostAttachmentLink(
    val type: Byte,
    val url: String,
)
