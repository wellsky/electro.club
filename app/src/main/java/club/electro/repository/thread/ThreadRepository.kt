package club.electro.repository.thread

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import club.electro.dto.Post
import club.electro.dto.PostsThread
import kotlinx.coroutines.flow.Flow

interface ThreadRepository {
    val thread: Flow<PostsThread>
    val lastUpdateTime: MutableLiveData<Long>

    fun posts(refreshTarget: ThreadLoadTarget): Flow<PagingData<Post>>

    suspend fun getThread()
    suspend fun changeSubscription(newStatus: Byte)

    suspend fun savePostToServer(post: Post)

    suspend fun removePost(post: Post)
    suspend fun checkForUpdates()

    fun startCheckUpdates()
    fun stopCheckUpdates()
}