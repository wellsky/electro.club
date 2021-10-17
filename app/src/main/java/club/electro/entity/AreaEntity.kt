package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import club.electro.dto.SubscriptionArea

@Entity
data class AreaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val image: String,
    val count: Int = 0,
    val last_name: String = "",
    val last_text: String,
    val last_time: Long = 0,
    val url: String = ""
) {
    fun toDto() = SubscriptionArea(
        id = id,
        image = image,
        name = name,
        last_text = last_text,
        last_time = last_time,
    )

    companion object {
        fun fromDto(dto: SubscriptionArea) =
            AreaEntity(id = dto.id, image = dto.image, name = dto.name, last_text = dto.last_text, last_time = dto.last_time)
    }
}

fun List<AreaEntity>.toDto(): List<SubscriptionArea> = map(AreaEntity::toDto)
fun List<SubscriptionArea>.toEntity(): List<AreaEntity> = map(AreaEntity::fromDto)