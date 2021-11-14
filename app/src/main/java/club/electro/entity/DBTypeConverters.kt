package club.electro.entity

import androidx.room.TypeConverter
import club.electro.dto.ThreadLink
import club.electro.dto.UserPrimaryTransport
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DBTypeConverters {
    @TypeConverter
    fun fromPrimaryTransport(primary: UserPrimaryTransport?): String? {
        val type = object : TypeToken<UserPrimaryTransport>() {}.type
        return Gson().toJson(primary, type)
    }

    @TypeConverter
    fun toPrimaryTransport(primaryString: String?): UserPrimaryTransport? {
        val type = object : TypeToken<UserPrimaryTransport>() {}.type
        return Gson().fromJson<UserPrimaryTransport>(primaryString, type)
    }

    @TypeConverter
    fun fromMyChat(primary: ThreadLink?): String? {
        val type = object : TypeToken<ThreadLink>() {}.type
        return Gson().toJson(primary, type)
    }

    @TypeConverter
    fun toMyChat(primaryString: String?): ThreadLink? {
        val type = object : TypeToken<ThreadLink>() {}.type
        return Gson().fromJson<ThreadLink>(primaryString, type)
    }
}