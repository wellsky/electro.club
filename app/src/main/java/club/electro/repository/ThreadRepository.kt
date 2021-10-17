package club.electro.repository

import club.electro.dto.Post
import kotlinx.coroutines.flow.Flow

interface ThreadRepository {
    val data: Flow<List<Post>>
    suspend fun getAll()
}