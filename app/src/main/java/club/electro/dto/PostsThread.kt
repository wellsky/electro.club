package club.electro.dto

import com.google.gson.TypeAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

data class PostsThread (
    val id: Long,
    val type: Byte,
    val name: String,
    val image: String,
    val messages: Long,
    @SerializedName("subscribers_count")  val subscribersCount: Long,
    @SerializedName("subscription_status")  val subscriptionStatus: Byte,
    @SerializedName("can_post")  val canPost: Boolean = false,
)

enum class ThreadType(val value: Byte) {
    THREAD_TYPE_PERSONAL_CHAT(1),
    THREAD_TYPE_PUBLIC_CHAT(2),
    THREAD_TYPE_CHANNEL(3),
    THREAD_TYPE_POST_WITH_COMMENTS(4);
}

val threadTypeSerializer = object : TypeAdapter<ThreadType>() {
    override fun write(out: JsonWriter, value: ThreadType?) {
        out.value(value?.value)
    }

    override fun read(`in`: JsonReader): ThreadType? =
        `in`.nextInt()
            .toByte()
            .let { serialized ->
                ThreadType.values().find {
                    it.value == serialized
                }
            }
}

const val SUBSCRIPTION_STATUS_NONE: Byte = 0;
const val SUBSCRIPTION_STATUS_SUBSCRIBED: Byte = 1;
const val SUBSCRIPTION_STATUS_IGONRING: Byte = 2;
const val SUBSCRIPTION_STATUS_MUTED: Byte = 3;
