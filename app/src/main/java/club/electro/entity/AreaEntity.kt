package club.electro.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import club.electro.dto.SubscriptionArea

@Entity
data class AreaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val type: Byte,
    val object_id: Long,
    val name: String,
    val image: String,
    val count: Int = 0,
    val last_name: String,
    val last_text: String,
    val last_time: Long = 0,
    val url: String = ""
) {
    fun toDto() = SubscriptionArea(
        id = id,
        type = type,
        object_id = object_id,
        image = image,
        name = name,
        last_name = last_name,
        last_text = last_text,
        last_time = last_time,
        count = count
    )

    companion object {
        fun fromDto(dto: SubscriptionArea) =
            AreaEntity(
                id = dto.id,
                type = dto.type,
                object_id = dto.object_id,
                image = dto.image,
                name = dto.name,
                last_name = dto.last_name,
                last_text = dto.last_text,
                last_time = dto.last_time,
                count = dto.count
            )
    }
}

fun List<AreaEntity>.toDto(): List<SubscriptionArea> = map(AreaEntity::toDto)
fun List<SubscriptionArea>.toEntity(): List<AreaEntity> = map(AreaEntity::fromDto)