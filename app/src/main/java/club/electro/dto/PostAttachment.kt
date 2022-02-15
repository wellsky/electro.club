package club.electro.dto

import com.google.gson.annotations.SerializedName

data class PostAttachment (
    val localId: Long,
    val id: Long?,
    @SerializedName("thread_type")  val threadType: Byte,
    @SerializedName("thread_id")  val threadId: Long,
    val type: Byte,
    val name: String?,
    val localPath: String?,
    @SerializedName("preview_url")  val previewUrl: String?,
    @SerializedName("full_url")  val fullUrl: String?,
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






