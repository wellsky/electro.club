package club.electro.dto

import com.google.gson.annotations.SerializedName

data class FeedPost (
    val id: Long,
    @SerializedName("channel_id")  val channelId: Long,
    @SerializedName("channel_name")  val channelName: String,
    @SerializedName("channel_avatar")  val channelAvatar: String,
    val image: String,
    val title: String? = null,
    val content: String,
    val published: Long,
    val sorting: Long,
    val views: Int,
    val comments: Int
)