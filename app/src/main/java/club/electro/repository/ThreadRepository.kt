package club.electro.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import club.electro.dto.Post
import club.electro.dto.PostsThread
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

interface ThreadRepository {
    val thread: Flow<PostsThread>
    val posts: Flow<PagingData<Post>>
    val lastUpdateTime: MutableLiveData<Long>

    suspend fun getThread()

    suspend fun savePost(post: Post)

    suspend fun removePost(post: Post)
    suspend fun checkForUpdates()

    fun reloadPosts(target: ThreadLoadTarget)
    fun changeTargetPost(target: ThreadLoadTarget)

    fun startCheckUpdates(): Job
    fun stopCheckUpdates()
}