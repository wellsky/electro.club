package club.electro.dto

import com.google.gson.annotations.SerializedName

data class Transport(
    val id: Long,
    val type: Byte,
    val name: String,
    val image: String?,
    val fullImage: String?,
    val users: Int,
    val rating: Float,
    val specs: TransportSpecs?,
)

data class TransportPreview(
    val id: Long,
    val type: Byte,
    val name: String,
    val image: String?,
    val users: Int,
    val rating: Float,
)

data class TransportSpecs(
    @SerializedName("weight") val weight: Float,
    @SerializedName("max_speed") val maxSpeed: Int,
    @SerializedName("max_distance") val maxDistance: Int,
    @SerializedName("voltage") val voltage: Int,
    @SerializedName("power") val power: Int,
    @SerializedName("") val batteryCapacityAh: Int,
)