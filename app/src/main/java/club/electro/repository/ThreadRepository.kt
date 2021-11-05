package club.electro.repository

import club.electro.dto.Post
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

interface ThreadRepository {
    val data: Flow<List<Post>>

    suspend fun getThreadPosts()
    suspend fun savePost(post: Post)
    suspend fun removePost(post: Post)
    suspend fun checkForUpdates()

    fun startCheckUpdates(): Job
    fun stopCheckUpdates()
}