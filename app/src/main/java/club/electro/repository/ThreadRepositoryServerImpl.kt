package club.electro.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.*
import club.electro.R
import club.electro.di.DependencyContainer
import club.electro.dto.Post
import club.electro.dto.PostsThread
import club.electro.entity.toEntity
import club.electro.error.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException

class ThreadRepositoryServerImpl(
            val diContainer: DependencyContainer,
            val threadType: Byte,
            val threadId: Long,
            val targetPost: ThreadLoadTarget = ThreadLoadTarget(targetPostPosition = ThreadLoadTarget.TARGET_POSITION_LAST)
        ) : ThreadRepository {

    private val threadDao = diContainer.appDb.threadDao()
    private val postDao = diContainer.appDb.postDao()
    private val userDao = diContainer.appDb.userDao()
    private val resources = diContainer.context.resources
    private val apiService = diContainer.apiService
    private val appAuth = diContainer.appAuth
    private val postRepository = diContainer.postRepository

    override val lastUpdateTime: MutableLiveData<Long> = MutableLiveData(0L)
    private val updaterJob = startCheckUpdates()

    // https://stackoverflow.com/questions/64692260/paging-3-0-list-with-new-params-in-kotlin?noredirect=1&lq=1
    var targetFlow = MutableStateFlow(value = targetPost)

    override val posts = targetFlow.flatMapLatest { refreshTarget ->
        @OptIn(ExperimentalPagingApi::class)
        Pager(
            config = PagingConfig(pageSize = 20),
            remoteMediator = PostRemoteMediator(diContainer, threadType, threadId, target = refreshTarget),
            pagingSourceFactory = {
                postDao.freshPosts(threadType, threadId)
            },
        ).flow.map { pagingData ->
            pagingData.map {
                it.toDto()
            }
        }
    }

    override val thread: Flow<PostsThread> = threadDao.get(threadType, threadId)

    override suspend fun getThread() {
        appAuth.myToken()?.let { myToken ->
            try {
                val response = apiService.getThread(
                    threadType = threadType,
                    threadId = threadId
                )

                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                threadDao.insert(body.data.thread.toEntity())
            } catch (e: IOException) {
                throw NetworkError
            } catch (e: Exception) {
                throw UnknownError
            }
        }
    }

//    override suspend fun unfreshThread() {
//        postDao.unfreshThread(threadType = threadType, threadId = threadId)
//    }

    override fun reloadPosts(target: ThreadLoadTarget) {
        changeTargetPost(target)

        // https://stackoverflow.com/questions/64715949/update-current-page-or-update-data-in-paging-3-library-android-kotlin
        println("reloadPosts()")
        //postDao.pagingSource(threadType, threadId).invalidate()
        // targetFlow.value = target
    }

    override fun changeTargetPost(target: ThreadLoadTarget) {
        targetFlow.value = target
    }

    override suspend fun savePostToServer(post: Post) {
        val newPost = post.copy(
            threadId = threadId,
            threadType = threadType,
        )
        postRepository.savePostToServer(newPost)
    }

    override suspend fun removePost(post: Post) {
        postRepository.removePostFromServer(post)
    }

    override suspend fun checkForUpdates()  {
        while (true) {
            delay(2_000L)

            val response = apiService.getAreaModifiedTime(
                type = threadType,
                objectId = threadId
            )
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            val newTime = body.data.time

            if (newTime > lastUpdateTime.value!!) {
                lastUpdateTime.postValue(newTime)
            }
        }
    }

    override fun startCheckUpdates(): Job {
        val job = CoroutineScope(Dispatchers.Default).launch {
            checkForUpdates()
        }
        return job
    }

    override fun stopCheckUpdates() {
        updaterJob.cancel()
    }
}


/**
 * Класс, описывающий задачу для загрузки поста
 * Передается в RemoteMediator чтобы загрузить посты от указанного в настройках
 * А также используется во фрагменте, чтобы после загрузки отобразить соответствующий пост
 */
class ThreadLoadTarget (
    val targetPostId: Long? = null,
    val targetPostPosition: String? = null,

    val quiet: Boolean = false,
    val highlight: Boolean = false,
) {
    companion object {
        val TARGET_POSITION_FIRST = "first"
        val TARGET_POSITION_LAST = "last"
    }

    fun targetApiParameter():String {
        if (targetPostId != null) {
            return targetPostId.toString()
        }
        return targetPostPosition ?: TARGET_POSITION_LAST
    }
}