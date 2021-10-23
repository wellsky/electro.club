package club.electro.dto

data class SubscriptionArea (
    val id: Long,
    val type: Byte,
    val object_id: Long,
    val name: String,
    val image: String,
    val count: Int = 0,
    val last_name: String,
    val last_text: String,
    val last_time: Long = 0,
    val url: String = ""
)