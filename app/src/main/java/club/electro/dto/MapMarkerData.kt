package club.electro.dto

import com.google.gson.annotations.SerializedName

data class MapMarkerData (
    val id: Long,
    val type: Byte,
    val lat: Double,
    val lng: Double,
    val icon: String?,
    val data: MarkerCustomData? = null
)

data class MarkerCustomData (
    @SerializedName("thread_type") val threadType: Byte? = null,
    @SerializedName("thread_id") val threadId: Long? = null
)

const val MARKER_TYPE_SOCKET: Byte = 6
const val MARKER_TYPE_GROUP: Byte = 10