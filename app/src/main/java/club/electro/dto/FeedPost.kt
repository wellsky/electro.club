package club.electro.dto

data class FeedPost (
    val id: Long,
    val authorName: String,
    val authorAvatar: String,
    val image: String,
    val text: String,
    val published: Long,
    val likes: Int,
    val views: Int,
    val comments: Int
)