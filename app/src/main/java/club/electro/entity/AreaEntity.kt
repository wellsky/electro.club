package club.electro.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import club.electro.dto.SubscriptionArea

@Entity
data class AreaEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val group: Byte = 0,
    val id: Long,
    val type: Byte,
    val objectId: Long,
    val name: String,
    val image: String,
    val count: Int = 0,
    val lastName: String,
    val lastText: String,
    val lastTime: Long = 0,
    val url: String = ""
) {
    fun toDto() = SubscriptionArea(
        id = id,
        group = group,
        type = type,
        objectId = objectId,
        image = image,
        name = name,
        lastName = lastName,
        lastText = lastText,
        lastTime = lastTime,
        count = count
    )

    companion object {
        fun fromDto(dto: SubscriptionArea) =
            AreaEntity(
                id = dto.id,
                group = dto.group,
                type = dto.type,
                objectId = dto.objectId,
                image = dto.image,
                name = dto.name,
                lastName = dto.lastName,
                lastText = dto.lastText,
                lastTime = dto.lastTime,
                count = dto.count
            )
    }
}

fun List<AreaEntity>.toDto(): List<SubscriptionArea> = map(AreaEntity::toDto)
fun List<SubscriptionArea>.toEntity(): List<AreaEntity> = map(AreaEntity::fromDto)