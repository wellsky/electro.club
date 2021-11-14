package club.electro.dto

open class User(
    val id: Long,
    val name: String,
    val avatar: String?,
    val messages: Int,
    val rating: Int,
    val primaryTransport: UserPrimaryTransport? = null,
    val myChat: ThreadLink? = null
)

// TODO правильно ли так наследоваться, делая UserProfile open? Или как лучше описать пустой профиль?
class EmptyUserProfile: User(
    id = 0,
    name = "",
    avatar = null,
    messages = 0,
    rating = 0
)

class UserPrimaryTransport (
    val id: Long,
    val name: String,
    val image: String,
)