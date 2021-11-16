package club.electro.repository

import androidx.work.WorkManager
import club.electro.dto.Post

interface PostRepository {
    fun setupWorkManager(workManager: WorkManager)

    suspend fun getLocalPostById(id: Long): Post?

    suspend fun savePostToChache(post: Post)

    suspend fun savePostToServer(post: Post)
    suspend fun savePostWork(localId: Long)

    suspend fun removePostFromServer(post: Post)
}