package club.electro.dto

import com.google.gson.annotations.SerializedName

data class Socket (
    val id: Long,
    val name: String,
    val text: String,
    val status: SocketStatus,
    val created: Long,
    val modified: Long,
    @SerializedName("author_id") val authorId: Long,
    @SerializedName("author_name") val authorName: String,
    @SerializedName("author_avatar") val authorAvatar: String,

    val images: List<String>?
)

enum class SocketStatus(value: String) {
    @SerializedName("on")
    ON("on"),

    @SerializedName("off")
    OFF("off"),

    @SerializedName("missing")
    MISSING("missing")
}

