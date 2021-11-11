package club.electro.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import club.electro.dto.Post
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ThreadRepository {
    val data: Flow<PagingData<Post>>

    suspend fun getThreadPosts()
    suspend fun savePost(post: Post)

    suspend fun removePost(post: Post)
    suspend fun checkForUpdates()

    fun changeData()

    fun startCheckUpdates(): Job
    fun stopCheckUpdates()
}