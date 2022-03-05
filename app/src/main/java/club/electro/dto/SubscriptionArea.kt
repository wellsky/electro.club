package club.electro.dto

import com.google.gson.annotations.SerializedName

data class SubscriptionArea (
    val id: Long,
    val group: Byte = 0,
    val type: Byte,
    @SerializedName("object_id")  val objectId: Long,
    val name: String,
    val image: String,
    val count: Int = 0,
    @SerializedName("last_name")  val lastName: String,
    @SerializedName("last_text")  val lastText: String,
    @SerializedName("last_time")  val lastTime: Long = 0,
    val url: String = ""
)