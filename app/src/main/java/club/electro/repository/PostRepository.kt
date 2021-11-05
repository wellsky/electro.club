package club.electro.repository

import androidx.work.WorkManager
import club.electro.dto.Post

interface PostRepository {
    fun setupWorkManager(workManager: WorkManager)

    suspend fun getLocalPostById(id: Long): Post?

    suspend fun savePost(post: Post)
    suspend fun savePostWork(localId: Long)

    suspend fun removePost(post: Post)
}