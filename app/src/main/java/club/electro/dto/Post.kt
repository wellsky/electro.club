package club.electro.dto

data class Post (
    val id: Long,
    val threadType: Byte,
    val threadId: Long,
    val authorId: Long,
    val authorName: String,
    val authorAvatar: String,
    val content: String,
    val published: Long,
    val likes: Int = 0,
    val views: Int = 0,
    //val ownedByMe: Boolean = false,
) {
    override fun equals(other: Any?): Boolean{
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Post
        return (this.id == other.id)
    }
}