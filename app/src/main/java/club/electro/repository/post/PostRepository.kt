package club.electro.repository.post

import androidx.work.WorkManager
import club.electro.dto.Post
import club.electro.entity.PostEntity

interface PostRepository {
    suspend fun getRemoteById(threadType: Byte, threadId:Long, id: Long): Post?
    suspend fun getLocalById(threadType: Byte, threadId:Long, id: Long, onLoadedCallback:  (suspend () -> Unit)? = null): Post?

    suspend fun prepareAndSaveLocal(postsEntities: List<PostEntity>)
    suspend fun prepareAndSaveLocal(postEntity: PostEntity)

    suspend fun savePostToServer(post: Post)
    suspend fun savePostWork(localId: Long)

    suspend fun removePostFromServer(post: Post)
}