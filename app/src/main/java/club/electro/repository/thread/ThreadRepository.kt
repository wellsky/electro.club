package club.electro.repository.thread

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import club.electro.dto.Post
import club.electro.dto.PostsThread
import club.electro.dto.ThreadLink
import kotlinx.coroutines.flow.Flow

interface ThreadRepository {
    val thread: Flow<PostsThread>
    val lastUpdateTime: MutableLiveData<Long>
    val threadStatus: MutableLiveData<ThreadStatus>

    fun posts(refreshTarget: ThreadLoadTarget): Flow<PagingData<Post>>

    suspend fun getThread()
    suspend fun setThreadVisit()
    suspend fun changeSubscription(newStatus: Byte)

    suspend fun savePostToServer(post: Post)

    suspend fun removePost(post: Post)
    suspend fun checkThreadUpdates()

//    fun startCheckUpdates()
//    fun stopCheckUpdates()
}

data class ThreadStatus (
    val lastUpdateTime: Long = 0,
    val lastMessageTime: Long = 0,
    val messagesCount: Long = 0,
)