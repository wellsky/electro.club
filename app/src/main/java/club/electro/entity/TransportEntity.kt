package club.electro.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import club.electro.dto.Transport
import club.electro.dto.TransportPreview
import club.electro.dto.TransportSpecs
import club.electro.dto.User

@Entity
data class TransportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val type: Byte,
    val name: String,
    val image: String? = null,
    val fullImage: String? = null,
    val users: Int,
    val rating: Float,
    val specs: TransportSpecs
) {
    fun toDto() = Transport(
        id = id,
        type = type,
        name = name,
        image = image,
        fullImage = fullImage,
        users = users,
        rating = rating,
        specs = specs,
    )
    fun toPreviewDto() = TransportPreview(
        id = id,
        type = type,
        name = name,
        image = image,
        users = users,
        rating = rating,
        specs = specs,
    )
    companion object {
        fun fromDto(dto: Transport) =
            TransportEntity(
                id = dto.id,
                type = dto.type,
                name = dto.name,
                image = dto.image,
                fullImage = dto.image,
                users = dto.users,
                rating = dto.rating,
                specs = dto.specs,
            )
        fun fromPreviewDto(dto: TransportPreview) =
            TransportEntity(
                id = dto.id,
                type = dto.type,
                name = dto.name,
                image = dto.image,
                users = dto.users,
                rating = dto.rating,
                specs = dto.specs,
            )
    }
}

fun List<TransportEntity>.toPreviewDto(): List<TransportPreview> = map(TransportEntity::toPreviewDto)
@JvmName("toPreviewEntity")
fun List<TransportPreview>.toEntity(): List<TransportEntity> = map(TransportEntity::fromPreviewDto)

fun List<TransportEntity>.toDto(): List<Transport> = map(TransportEntity::toDto)
@JvmName("toFullEntity")
fun List<Transport>.toEntity(): List<TransportEntity> = map(TransportEntity::fromDto)

fun Transport.toEntity(): TransportEntity = TransportEntity.fromDto(this)