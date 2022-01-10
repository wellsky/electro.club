package club.electro.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import club.electro.dto.Post
import club.electro.dto.PostsThread
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

interface ThreadRepository {
    val thread: Flow<PostsThread>
    val threadStatus: MutableLiveData<ThreadStatus>
    val posts: Flow<PagingData<Post>>


    suspend fun getThread()
    suspend fun changeSubscription(newStatus: Byte)

    suspend fun savePostToServer(post: Post)

    suspend fun removePost(post: Post)
    suspend fun checkForUpdates()

    fun reloadPosts(target: ThreadLoadTarget)
    fun changeTargetPost(target: ThreadLoadTarget)

    fun startCheckUpdates()
    fun stopCheckUpdates()
}

data class ThreadStatus (
    val lastUpdateTime: Long = 0,
    val lastMessageTime: Long = 0,
)