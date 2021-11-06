package club.electro.dto

data class FeedPost (
    val id: Long,
    val channelId: Long,
    val channelName: String,
    val channelAvatar: String,
    val image: String,
    val title: String,
    val content: String,
    val published: Long,
    val sorting: Long,
    val likes: Int,
    val views: Int,
    val comments: Int
)