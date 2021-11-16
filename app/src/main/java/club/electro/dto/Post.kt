package club.electro.dto

data class Post (
    val localId: Long,
    val id: Long,
    val status: Byte,
    val threadType: Byte,
    val threadId: Long,
    val authorId: Long,
    val authorName: String,
    val authorAvatar: String,
    val content: String,
    val preparedContent: String? = content,
    val published: Long,
    val answerTo: Long? = null,
    val likes: Int = 0,
    val views: Int = 0,

    val canEdit: Boolean = false,
    val canRemove: Boolean = false
) {
    companion object {
        const val STATUS_PUBLISHED: Byte = 0
        const val STATUS_CREATED_LOCAL: Byte = 101
        const val STATUS_SAVING_LOCAL: Byte = 102
        const val STATUS_REMOVING_LOCAL: Byte = 103
    }
}
