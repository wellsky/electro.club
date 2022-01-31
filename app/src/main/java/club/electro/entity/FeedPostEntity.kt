package club.electro.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import club.electro.dto.FeedPost
import club.electro.dto.Post

@Entity
data class FeedPostEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val channelId: Long,
    val channelName: String,
    val channelAvatar: String,
    val title: String?,
    val content: String,
    val image: String,
    val published: Long,
    val sorting: Long,
    val likes: Int,
    val views: Int,
    val comments: Int,
) {
    fun toDto() = FeedPost(
        id = id,
        channelId = channelId,
        channelName = channelName,
        channelAvatar = channelAvatar,
        title = title,
        content = content,
        image = image,
        published = published,
        sorting = sorting,
        likes = likes,
        views = views,
        comments = comments
    )

    companion object {
        fun fromDto(dto: FeedPost) =
            FeedPostEntity(
                id = dto.id,
                channelId = dto.channelId,
                channelName = dto.channelName,
                channelAvatar = dto.channelAvatar,
                title = dto.title,
                content = dto.content,
                image = dto.image,
                published = dto.published,
                sorting = dto.sorting,
                likes = dto.likes,
                views = dto.views,
                comments = dto.comments,
            )
    }
}

fun List<FeedPostEntity>.toDto(): List<FeedPost> = map(FeedPostEntity::toDto)
fun List<FeedPost>.toEntity(): List<FeedPostEntity> = map(FeedPostEntity::fromDto)