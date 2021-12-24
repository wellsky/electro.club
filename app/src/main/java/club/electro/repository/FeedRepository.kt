package club.electro.repository

import club.electro.dto.FeedPost
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    val data: Flow<List<FeedPost>>

    suspend fun getFeedPosts()
}