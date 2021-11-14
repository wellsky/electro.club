package club.electro.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import club.electro.dto.Post
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

interface ThreadRepository {
    val data: Flow<PagingData<Post>>
    val lastUpdateTime: MutableLiveData<Long>

    //suspend fun getThreadPosts()
    suspend fun savePost(post: Post)

    suspend fun removePost(post: Post)
    suspend fun checkForUpdates()

    fun reloadPosts()
    fun changeTargetPost(target: ThreadTargetPost)

    fun startCheckUpdates(): Job
    fun stopCheckUpdates()
}