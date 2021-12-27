package club.electro.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import club.electro.dto.MapMarker
import club.electro.dto.MarkerCustomData

@Entity
data class MapMarkerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val type: Byte,
    val lat: Double,
    val lng: Double,
    val icon: String?,
    val data: MarkerCustomData?
) {
    fun toDto() = MapMarker(
        id = id,
        type = type,
        lat = lat,
        lng = lng,
        icon = icon,
        data = data,
    )

    companion object {
        fun fromDto(dto: MapMarker) =
            MapMarkerEntity(
                id = dto.id,
                type = dto.type,
                lat = dto.lat,
                lng = dto.lng,
                icon = dto.icon,
                data = dto.data,
            )
    }
}

fun List<MapMarkerEntity>.toDto(): List<MapMarker> = map(MapMarkerEntity::toDto)
fun List<MapMarker>.toEntity(): List<MapMarkerEntity> = map(MapMarkerEntity::fromDto)