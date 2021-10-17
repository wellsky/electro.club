package club.electro.dto

data class Post (
    val id: Long,
    val authorId: Long,
    val authorName: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likes: Int = 0,
    val views: Int = 0,
    val ownedByMe: Boolean = false,
)