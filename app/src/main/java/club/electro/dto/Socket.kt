package club.electro.dto

import com.google.gson.annotations.SerializedName

data class Socket (
    val id: Long,
    val name: String,
    val text: String,
    val status: String,
    val created: Long,
    val modified: Long,
    @SerializedName("author_id") val authorId: Long,
    @SerializedName("author_name") val authorName: String,
    @SerializedName("author_avatar") val authorAvatar: String
)
