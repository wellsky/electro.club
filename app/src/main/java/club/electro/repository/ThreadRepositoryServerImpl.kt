package club.electro.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.*
import club.electro.R
import club.electro.di.DependencyContainer
import club.electro.dto.Post
import club.electro.dto.PostsThread
import club.electro.entity.toEntity
import club.electro.error.*
import club.electro.model.NetworkStatus
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
    private val apiService = diContainer.apiService
    private val appAuth = diContainer.appAuth
    private val postRepository = diContainer.postRepository
    private val networkStatus = diContainer.networkStatus

    override val lastUpdateTime: MutableLiveData<Long> = MutableLiveData(0L)

    private lateinit var updaterJob: Job

    // https://stackoverflow.com/questions/64692260/paging-3-0-list-with-new-params-in-kotlin?noredirect=1&lq=1
    var targetFlow = MutableStateFlow(value = targetPost)

    override val posts = targetFlow.flatMapLatest { refreshTarget ->
        println("Creating pager")

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
                networkStatus.setStatus(NetworkStatus.Status.ONLINE)
            } catch (e: IOException) {
                networkStatus.setStatus(NetworkStatus.Status.ERROR)
            } catch (e: Exception) {

            }
        }
    }

    override fun reloadPosts(target: ThreadLoadTarget) {
        changeTargetPost(target)
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
            try {
                val response = apiService.getAreaModifiedTime(
                    type = threadType,
                    objectId = threadId
                )
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }

                val body = response.body() //?: throw ApiError(response.code(), response.message())

                body?.let {
                    val newTime = body.data.time

                    if (newTime > lastUpdateTime.value!!) {
                        lastUpdateTime.postValue(newTime)
                    }

                    networkStatus.setStatus(NetworkStatus.Status.ONLINE)
                }
            }  catch (e: IOException) {
                networkStatus.setStatus(NetworkStatus.Status.ERROR)
            } catch (e: Exception) {
                // throw UnknownError
            }
        }
    }

    override fun startCheckUpdates() {
        updaterJob = CoroutineScope(Dispatchers.Default).launch {
            checkForUpdates()
        }
    }

    override fun stopCheckUpdates() {
        updaterJob.cancel()
    }
}


/**
 * Класс, описывающий задачу для загрузки чата, начиная с определенного поста
 * Передается в RemoteMediator чтобы загрузить посты от указанного в настройках
 * А также используется во фрагменте, чтобы после загрузки отобразить соответствующий пост
 */
class ThreadLoadTarget (
    val targetPostId: Long? = null,
    val targetPostPosition: String? = null,
) {
    companion object {
        val TARGET_POSITION_FIRST = "first"
        val TARGET_POSITION_LAST = "last"
        val TARGET_POSITION_FIRST_UNREAD = "first_unread"
    }

    fun targetApiParameter():String {
        if (targetPostId != null) {
            return targetPostId.toString()
        }
        return targetPostPosition ?: TARGET_POSITION_LAST
    }
}