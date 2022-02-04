package club.electro.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import club.electro.dto.TransportPreview

@Entity
data class TransportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val type: Byte,
    val name: String,
    val image: String,
    val fullImage: String? = null,
    val users: Int,
    val rating: Float,
) {
    fun toPreviewDto() = TransportPreview(
        id = id,
        type = type,
        name = name,
        image = image,
        users = users,
        rating = rating,
    )

    companion object {
        fun fromPreviewDto(dto: TransportPreview) =
            TransportEntity(
                id = dto.id,
                type = dto.type,
                name = dto.name,
                image = dto.image,
                users = dto.users,
                rating = dto.rating
            )
    }
}

fun List<TransportEntity>.toPreviewDto(): List<TransportPreview> = map(TransportEntity::toPreviewDto)
fun List<TransportPreview>.toEntity(): List<TransportEntity> = map(TransportEntity::fromPreviewDto)