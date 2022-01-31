package club.electro.dto

import com.google.gson.annotations.SerializedName

open class User(
    @SerializedName("user_id") val id: Long,
    @SerializedName("nickname") val name: String,
    @SerializedName("thumbnail") val avatar: String? = null,
    @SerializedName("messages") val messages: Int = 0,
    @SerializedName("account_created") val created: Long,
    @SerializedName("last_visit")  val lastVisit: Long,
    @SerializedName("rating")  val rating: Int = 0,
    @SerializedName("primary_transport")  val primaryTransport: UserPrimaryTransport? = null,
    @SerializedName("my_chat")  val myChat: ThreadLink? = null
)

class UserPrimaryTransport (
    val id: Long,
    val name: String,
    val image: String,
)