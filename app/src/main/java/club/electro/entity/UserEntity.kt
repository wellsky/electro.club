package club.electro.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import club.electro.dto.ThreadLink
import club.electro.dto.User
import club.electro.dto.UserPrimaryTransport
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Как работают embedded и typeconverters
 * https://medium.com/@nemanja.stamenovic/how-to-use-room-with-nested-json-81dec1df1908
 */

@Entity
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val avatar: String?,
    val messages: Int,
    val rating: Int,
    val primaryTransport: UserPrimaryTransport?,
    val myChat: ThreadLink?,
) {
    fun toDto() = User(
        id = id,
        name = name,
        avatar = avatar,
        messages = messages,
        rating = rating,
        primaryTransport = primaryTransport,
        myChat = myChat
    )

    companion object {
        fun fromDto(dto: User) =
            UserEntity(
                id = dto.id,
                name = dto.name,
                avatar = dto.avatar,
                messages = dto.messages,
                rating = dto.rating,
                primaryTransport = dto.primaryTransport,
                myChat = dto.myChat
            )
    }
}

fun List<UserEntity>.toDto(): List<User> = map(UserEntity::toDto)
fun List<User>.toEntity(): List<UserEntity> = map(UserEntity::fromDto)
fun User.toEntity(): UserEntity = UserEntity.fromDto(this)

//data class UserPrimaryTransportEmbeddable(
//    var id: Long,
//    var name: String,
//    val image: String
//) {
//    fun toDto() = UserPrimaryTransport(id, name, image)
//
//    companion object {
//        fun fromDto(dto: UserPrimaryTransport?) = dto?.let {
//            UserPrimaryTransportEmbeddable(it.id, it.name, it.image)
//        }
//    }
//}