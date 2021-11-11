package club.electro.repository

import androidx.paging.*
import androidx.work.*
import club.electro.R
import club.electro.adapter.PostTextPreparator
import club.electro.di.DependencyContainer
import club.electro.dto.Post
import club.electro.entity.PostEntity
import club.electro.entity.toDto
import club.electro.entity.toEntity
import java.io.IOException
import club.electro.error.*
import club.electro.workers.SavePostWorker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.concurrent.thread

class ThreadRepositoryServerImpl(
            diContainer: DependencyContainer,
            val threadType: Byte,
            val threadId: Long
        ) : ThreadRepository {

    private val dao = diContainer.appDb.postDao()
    private val resources = diContainer.context.resources
    private val apiService = diContainer.apiService
    private val appAuth = diContainer.appAuth
    private val postRepository = diContainer.postRepository

//    override var data: Flow<List<Post>> = dao.flowThreadByPublshedDESC(threadType, threadId).map(List<PostEntity>::toDto).flowOn(Dispatchers.Default)


//    @OptIn(ExperimentalPagingApi::class)
//    val data1: Flow<PagingData<Post>> = Pager(
//        config = PagingConfig(pageSize = 25),
//        remoteMediator = PostRemoteMediator(apiService, appDb, postDao, postRemoteKeyDao),
//        pagingSourceFactory = postDao::pagingSource,
//    ).flow.map { pagingData ->
//        pagingData.map(PostEntity::toDto)
//    }



    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 20),
        remoteMediator = PostRemoteMediator(diContainer, threadType, threadId),
        pagingSourceFactory = {
            dao.pagingSource(threadType, threadId)
        },
    ).flow.map { pagingData ->
        pagingData.map {
            val post = it.toDto()

            val preparedContent: String = PostTextPreparator(post)
                .prepareAll()
                .get()

            val preparedPost = post.copy(preparedContent = preparedContent)
            preparedPost
        }
    }


//        pagingData.map {
//            val post = it.toDto()
//
//            val preparedContent: String = PostTextPreparator(post)
//                .prepareAll()
//                .get()
//
//            val preparedPost = post.copy(preparedContent = preparedContent)
//            preparedPost
//        }
//    }


//    @OptIn(ExperimentalPagingApi::class)
//    override var data: Flow<List<Post>> = dao.flowThreadByPublshedDESC(threadType, threadId).map {
//        it.map {
//            //println("Preparing post " + it.id)
//            val post = it.toDto()
//
//            val preparedContent: String = PostTextPreparator(post)
//                .prepareAll()
//                .get()
//
//            val preparedPost = post.copy(preparedContent = preparedContent)
//            preparedPost
//        }
//    }.flowOn(Dispatchers.Default)

    private var lastUpdateTime: Long = 0

    private val updaterJob = startCheckUpdates()

    // TODO возможно, этот метод бльше не нужен? (после внедрения Pager)
    override suspend fun getThreadPosts() {
        try {
            //println("Loading posts from server")
//            val params = HashMap<String?, String?>()
//            params["access_token"] = resources.getString(R.string.electro_club_access_token)
//            params["user_token"] = appAuth.myToken()
//            params["method"] = "getPosts"
//            params["thread_type"] = threadType.toString()
//            params["thread_id"] = threadId.toString()
//            val response = apiService.getThreadPosts(params)

            val response = apiService.getThreadPosts(
                access_token = resources.getString(R.string.electro_club_access_token),
                user_token = appAuth.myToken(),
                threadType = threadType,
                threadId = threadId,
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            dao.insert(body.data.messages.toEntity())

            // TODO перенести в пагинатор. Период для удаления сообщений брать из запроса, а не ответа.
//            val currentMessages = dao.getAllList(threadType, threadId).toDto()
//            if (currentMessages.isEmpty()) {
//                dao.insert(body.data.messages.toEntity())
//            } else {
//                val first = currentMessages.first()
//                val last = currentMessages.last()
//
//
//                val last = body.data.messages.first()
//                val first = body.data.messages.last()
//                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
//                val date1 = java.util.Date(first.published * 1000)
//                val date2 = java.util.Date(last.published * 1000)
//                println("first: " + sdf.format(date1).toString())
//                println("last: " + sdf.format(date2).toString())
//                dao.clearAndInsert(body.data.messages.toEntity(), threadType, threadId, first.published, last.published)
//            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun savePost(post: Post) {
        val newPost = post.copy(
            threadId = threadId,
            threadType = threadType,
        )
        postRepository.savePost(newPost)
    }

    override suspend fun removePost(post: Post) {
        postRepository.removePost(post)
    }

    override suspend fun checkForUpdates()  {
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

            if (newTime > lastUpdateTime) {
                if (lastUpdateTime != 0L) getThreadPosts()
                lastUpdateTime = newTime
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