package club.electro.dto

data class TransportPreview(
    val id: Long,
    val type: Byte,
    val name: String,
    val image: String,
    val users: Int,
    val rating: Float,
)