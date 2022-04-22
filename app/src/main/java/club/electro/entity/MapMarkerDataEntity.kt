package club.electro.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import club.electro.dto.MapMarkerData
import club.electro.dto.MarkerCustomData

@Entity
data class MapMarkerDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val type: Byte,
    val lat: Double,
    val lng: Double,
    val icon: String?,
    val data: MarkerCustomData?
) {
    fun toDto() = MapMarkerData(
        id = id,
        type = type,
        lat = lat,
        lng = lng,
        icon = icon,
        data = data,
    )

    companion object {
        fun fromDto(dto: MapMarkerData) =
            MapMarkerDataEntity(
                id = dto.id,
                type = dto.type,
                lat = dto.lat,
                lng = dto.lng,
                icon = dto.icon,
                data = dto.data,
            )
    }
}

fun List<MapMarkerDataEntity>.toDto(): List<MapMarkerData> = map(MapMarkerDataEntity::toDto)
fun List<MapMarkerData>.toEntity(): List<MapMarkerDataEntity> = map(MapMarkerDataEntity::fromDto)