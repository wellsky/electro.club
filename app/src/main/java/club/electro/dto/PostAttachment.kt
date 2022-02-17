package club.electro.dto

import com.google.gson.annotations.SerializedName

data class PostAttachment (
    val localId: Long,
    val id: Long?,
    @SerializedName("thread_type")  val threadType: Byte,
    @SerializedName("thread_id")  val threadId: Long,
    @SerializedName("post_id")  val postId: Long = 0,
    val type: Byte,
    val name: String?,
    val localPath: String?,
    @SerializedName("preview_url")  val previewUrl: String?,
    @SerializedName("full_url")  val fullUrl: String?,
    val created: Long,
    val status: Byte = STATUS_UNKNOWN,
) {
    companion object {
        const val STATUS_UNKNOWN: Byte = 0
        const val STATUS_CREATED: Byte = 1
        const val STATUS_READY_TO_UPLOAD: Byte = 2
        const val STATUS_COMPRESSING: Byte = 3
        const val STATUS_UPLOADING: Byte = 4
        const val STATUS_UPLOADED: Byte = 5

        const val STATUS_ERROR_UNKNOWN: Byte = -1
        const val STATUS_ERROR_NOT_FOUND: Byte = -2
        const val STATUS_ERROR_COMPRESSING: Byte = -3
        const val STATUS_ERROR_UPLOADING: Byte = -4
    }
}






