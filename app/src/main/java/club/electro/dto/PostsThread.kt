package club.electro.dto

data class PostsThread (
    val id: Long,
    val type: Byte,
    val name: String,
    val image: String,
    val messages: Long,
    val subscribersCount: Long,
)
