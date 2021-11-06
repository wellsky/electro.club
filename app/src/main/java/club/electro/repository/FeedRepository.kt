package club.electro.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import club.electro.dto.FeedPost
import club.electro.dto.Post
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    val data: Flow<List<FeedPost>>

    suspend fun getFeedPosts()
}