package club.electro.dto

import com.google.gson.annotations.SerializedName

data class ThreadLink (
    @SerializedName("thread_type")  val threadType: Byte,
    @SerializedName("thread_id")  val threadId: Long,
)