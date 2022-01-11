package club.electro.dto

data class PostsThread (
    val id: Long,
    val type: Byte,
    val name: String,
    val image: String,
    val messages: Long,
    val subscribersCount: Long,
    val subscriptionStatus: Byte
)

enum class ThreadType(val value: Byte) {
    THREAD_TYPE_PERSONAL_CHAT(1),
    THREAD_TYPE_PUBLIC_CHAT(2),
    THREAD_TYPE_CHANNEL(3),
    THREAD_TYPE_POST_WITH_COMMENTS(4);
}

//const val THREAD_TYPE_PERSONAL_CHAT: Byte = 1
//const val THREAD_TYPE_PUBLIC_CHAT: Byte = 2
//const val THREAD_TYPE_CHANNEL: Byte = 3
//const val THREAD_TYPE_POST_WITH_COMMENTS: Byte = 4

const val SUBSCRIPTION_STATUS_NONE: Byte = 0;
const val SUBSCRIPTION_STATUS_SUBSCRIBED: Byte = 1;
const val SUBSCRIPTION_STATUS_IGONRING: Byte = 2;
const val SUBSCRIPTION_STATUS_MUTED: Byte = 3;
