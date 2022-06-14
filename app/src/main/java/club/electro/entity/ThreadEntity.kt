package club.electro.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import club.electro.dto.PostsThread
import club.electro.dto.ThreadHeaderMessage
import club.electro.dto.ThreadType
import club.electro.dto.User
import club.electro.utils.toPlainText
import com.google.gson.annotations.SerializedName

@Entity
data class ThreadEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val type: Byte,
    val name: String,
    val image: String,
    val messages: Long,
    val headerMessage: ThreadHeaderMessage?,
    val subscribersCount: Long,
    val subscriptionStatus: Byte,
    val canPost: Boolean = false,
) {
    fun toDto() = PostsThread(
        id = id,
        type = type,
        name = name,
        image = image,
        messages = messages,
        headerMessage = headerMessage,
        subscribersCount = subscribersCount,
        subscriptionStatus = subscriptionStatus,
        canPost = canPost,
    )

    companion object {
        fun fromDto(dto: PostsThread) =
            ThreadEntity(
                id = dto.id,
                type = dto.type,
                name = dto.name,
                image = dto.image,
                messages = dto.messages,
                headerMessage = dto.headerMessage?.fromDto(),
                subscribersCount = dto.subscribersCount,
                subscriptionStatus = dto.subscriptionStatus,
                canPost = dto.canPost
            )
    }
}

fun List<ThreadEntity>.toDto(): List<PostsThread> = map(ThreadEntity::toDto)
fun List<PostsThread>.toEntity(): List<ThreadEntity> = map(ThreadEntity::fromDto)
fun PostsThread.toEntity(): ThreadEntity = ThreadEntity.fromDto(this)

private fun ThreadHeaderMessage.fromDto() : ThreadHeaderMessage =
    this.copy(
        text = this.text.toPlainText()
    )
