package club.electro.dto

open class User(
    val id: Long,
    val name: String,
    val avatar: String? = null,
    val messages: Int = 0,
    val rating: Int = 0,
    val primaryTransport: UserPrimaryTransport? = null,
    val myChat: ThreadLink? = null
)

class UserPrimaryTransport (
    val id: Long,
    val name: String,
    val image: String,
)