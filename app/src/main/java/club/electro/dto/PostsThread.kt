package club.electro.dto

data class PostsThread (
    val id: Long,
    val type: Byte,
    val name: String,
    val image: String,
    val messages: Long,
    val subscribersCount: Long,
)

const val THREAD_TYPE_PERSONAL_CHAT: Byte = 1
const val THREAD_TYPE_PUBLIC_CHAT: Byte = 2
const val THREAD_TYPE_CHANNEL: Byte = 3
const val THREAD_TYPE_POST_WITH_COMMENTS: Byte = 4
