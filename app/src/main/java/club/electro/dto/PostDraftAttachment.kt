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
) {
    companion object {
        const val STATUS_CREATED: Byte = 0
        const val STATUS_READY_TO_UPLOAD: Byte = 1
        const val STATUS_UPLOADING: Byte = 2
        const val STATUS_UPLOADED: Byte = 3

        const val STATUS_ERROR: Byte = -1
    }
}






