package club.electro.entity

import androidx.room.TypeConverter
import club.electro.dto.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DBTypeConverters {
    @TypeConverter
    fun fromStrings(primary: List<String>?): String? {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().toJson(primary, type)
    }

    @TypeConverter
    fun toStrings(primaryString: String?): List<String>? {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson<List<String>>(primaryString, type)
    }

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

    @TypeConverter
    fun fromMarkerCustomData(primary: MarkerCustomData?): String? {
        val type = object : TypeToken<MarkerCustomData>() {}.type
        return Gson().toJson(primary, type)
    }

    @TypeConverter
    fun toMarkerCustomData(primaryString: String?): MarkerCustomData? {
        val type = object : TypeToken<MarkerCustomData>() {}.type
        return Gson().fromJson<MarkerCustomData>(primaryString, type)
    }

    @TypeConverter
    fun fromPostAttachments(primary: List<PostAttachmentLink>?): String? {
        val type = object : TypeToken<List<PostAttachmentLink>>() {}.type
        return Gson().toJson(primary, type)
    }

    @TypeConverter
    fun toPostAttachments(primaryString: String?): List<PostAttachmentLink>? {
        val type = object : TypeToken<List<PostAttachmentLink>>() {}.type
        return Gson().fromJson<List<PostAttachmentLink>>(primaryString, type)
    }

    @TypeConverter
    fun fromTransportSpecs(primary: TransportSpecs?): String? {
        val type = object : TypeToken<TransportSpecs>() {}.type
        return Gson().toJson(primary, type)
    }

    @TypeConverter
    fun toTransportSpecs(primaryString: String?): TransportSpecs? {
        val type = object : TypeToken<TransportSpecs>() {}.type
        return Gson().fromJson<TransportSpecs>(primaryString, type)
    }
}