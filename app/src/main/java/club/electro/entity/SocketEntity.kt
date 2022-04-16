package club.electro.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import club.electro.dto.Post
import club.electro.dto.Socket
import club.electro.dto.SocketStatus

@Entity
data class SocketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val text: String,
    val status: SocketStatus,
    val created: Long,
    val modified: Long,
    val authorId: Long,
    val authorName: String,
    val authorAvatar: String,
    val images: List<String>?
) {
    fun toDto() = Socket (
        id = id,
        name = name,
        text = text,
        status = status,
        created = created,
        modified = modified,
        authorId = authorId,
        authorName = authorName,
        authorAvatar = authorAvatar,
        images = images,
    )

    companion object {
        fun fromDto(dto: Socket) =
            SocketEntity(
                id = dto.id,
                name = dto.name,
                text = dto.text,
                status = dto.status,
                created = dto.created,
                modified = dto.modified,
                authorId = dto.authorId,
                authorName = dto.authorName,
                authorAvatar = dto.authorAvatar,
                images = dto.images
            )
    }
}

fun Socket.toEntity(): SocketEntity = SocketEntity.fromDto(this)

