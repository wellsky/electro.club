package club.electro.repository

import androidx.work.WorkManager
import club.electro.dto.Post

interface PostRepository {
    fun setupWorkManager(workManager: WorkManager)

    suspend fun getRemoteById(threadType: Byte, threadId:Long, id: Long): Post
    suspend fun getLocalById(threadType: Byte, threadId:Long, id: Long, onLoadedCallback:  (suspend () -> Unit)? = null): Post?


    suspend fun savePostToChache(post: Post)
    suspend fun updateLocalPostPreparedContent(threadType: Byte, threadId: Long, id: Long, preparedContent: String)

    suspend fun savePostToServer(post: Post)
    suspend fun savePostWork(localId: Long)

    suspend fun removePostFromServer(post: Post)
}