package club.electro.dto

data class TransportPreview(
    val id: Long,
    val name: String,
    val image: String,
    val users: Int,
    val rating: Float,
)