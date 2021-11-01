package club.electro.dto

open class UserProfile(
    val id: Long,
    val name: String,
    val avatar: String?,
    val messages: Int,
    val rating: Int
)

// TODO правильно ли так наследоваться, делая UserProfile open? Или как лучше описать пустой профиль?
class EmptyUserProfile: UserProfile(
    id = 0,
    name = "",
    avatar = null,
    messages = 0,
    rating = 0
)