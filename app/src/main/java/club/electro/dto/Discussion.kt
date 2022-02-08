package club.electro.dto

import com.google.gson.annotations.SerializedName

data class Discussion (
    @SerializedName("transport_id") val transportId: Long,
    @SerializedName("series_id") val seriesId: Long,
    @SerializedName("thread_type") val threadType: Byte,
    @SerializedName("thread_id") val threadId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("image") val image: String,
    @SerializedName("messages") val messages: Long,
    @SerializedName("last_message_time") val lastMessageTime: Long,
)