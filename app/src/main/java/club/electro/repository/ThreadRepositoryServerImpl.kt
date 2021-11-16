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
            val threadId: Long
        ) : ThreadRepository {

    private val threadDao = diContainer.appDb.threadDao()
    private val postDao = diContainer.appDb.postDao()
    private val userDao = diContainer.appDb.userDao()
    private val resources = diContainer.context.resources
    private val apiService = diContainer.apiService
    private val appAuth = diContainer.appAuth
    private val postRepository = diContainer.postRepository

//    override var data: Flow<List<Post>> = dao.flowThreadByPublshedDESC(threadType, threadId).map(List<PostEntity>::toDto).flowOn(Dispatchers.Default)

    override val lastUpdateTime: MutableLiveData<Long> = MutableLiveData(0L)
    private val updaterJob = startCheckUpdates()

//    @OptIn(ExperimentalPagingApi::class)
//    val data1: Flow<PagingData<Post>> = Pager(
//        config = PagingConfig(pageSize = 25),
//        remoteMediator = PostRemoteMediator(apiService, appDb, postDao, postRemoteKeyDao),
//        pagingSourceFactory = postDao::pagingSource,
//    ).flow.map { pagingData ->
//        pagingData.map(PostEntity::toDto)
//    }


    // https://stackoverflow.com/questions/64692260/paging-3-0-list-with-new-params-in-kotlin?noredirect=1&lq=1
    var targetFlow = MutableStateFlow(value = ThreadLoadTarget(targetPostPosition = "last"))
    //val pagingSource = dao.pagingSource(threadType, threadId)

//    val dataSourceFactory = object : PagingSource<Int, PostEntity>() {
//        fun create(): PagingSource<Int, PostEntity> {
//            return dao.pagingSource(threadType, threadId)
//        }
//
//        override fun getRefreshKey(state: PagingState<Int, PostEntity>): Int? {
//            TODO("Not yet implemented")
//        }
//
//        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PostEntity> {
//            TODO("Not yet implemented")
//        }
//    }

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
                val post = it.toDto()

//                val user = User(
//                    id = it.authorId,
//                    name = it.authorName,
//                    avatar = it.authorAvatar
//                )
//                userDao.insertIfNotExists(user.toEntity())

//                val preparedContent: String = PostTextPreparator(post)
//                    .prepareAll()
//                    .get()
//
//                val preparedPost = post.copy(preparedContent = preparedContent)
//                preparedPost
                post
            }
        }
    }

    override val thread: Flow<PostsThread> = threadDao.get(threadType, threadId)

    override suspend fun getThread() {
        appAuth.myToken()?.let { myToken ->
            try {
                val response = apiService.getThread(
                    access_token = resources.getString(R.string.electro_club_access_token),
                    user_token = myToken,
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

    override fun reloadPosts(target: ThreadLoadTarget) {
        // https://stackoverflow.com/questions/64715949/update-current-page-or-update-data-in-paging-3-library-android-kotlin
        println("reloadPosts()")
        //postDao.pagingSource(threadType, threadId).invalidate()
        targetFlow.value = target
    }

    override fun changeTargetPost(target: ThreadLoadTarget) {
        targetFlow.value = target
    }

//    // TODO возможно, этот метод бльше не нужен? (после внедрения Pager)
//    override suspend fun getThreadPosts() {
//        try {
//            val response = apiService.getThreadPosts(
//                access_token = resources.getString(R.string.electro_club_access_token),
//                user_token = appAuth.myToken(),
//                threadType = threadType,
//                threadId = threadId,
//            )
//
//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
//            val body = response.body() ?: throw ApiError(response.code(), response.message())
//
//            dao.insert(body.data.messages.toEntity())
//        } catch (e: IOException) {
//            throw NetworkError
//        } catch (e: Exception) {
//            throw UnknownError
//        }
//    }

    override suspend fun saveToServerPost(post: Post) {
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
        println("Start checking updates...")
        while (false) {
            delay(2_000L)

            val params = HashMap<String?, String?>()
            params["access_token"] = resources.getString(R.string.electro_club_access_token)
            params["user_token"] = appAuth.myToken()
            params["method"] = "getAreaModifiedTime"
            params["type"] = threadType.toString()
            params["object_id"] = threadId.toString()

            val response = apiService.getAreaModifiedTime(params)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            val newTime = body.data.time

            if (newTime > lastUpdateTime.value!!) {
                //refreshData()
                //if (lastUpdateTime != 0L) getThreadPosts()
                lastUpdateTime.postValue(newTime)
            }
            //println("lastUpdate: " + lastUpdateTime + ", newTime: " + newTime)
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
    val targetPostPosition: String? = TARGET_POSITION_LAST,

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