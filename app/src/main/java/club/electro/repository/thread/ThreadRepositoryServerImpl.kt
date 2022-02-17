package club.electro.repository.thread

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.paging.*
import androidx.room.withTransaction
import club.electro.api.ApiService
import club.electro.auth.AppAuth
import club.electro.dao.PostAttachmentDao
import club.electro.dao.PostDao
import club.electro.dao.ThreadDao
import club.electro.db.AppDb
import club.electro.dto.Post
import club.electro.dto.PostAttachment
import club.electro.dto.PostsThread
import club.electro.entity.toEntity
import club.electro.error.*
import club.electro.model.NetworkStatus
import club.electro.repository.PostRemoteMediatorFactory
import club.electro.repository.post.PostRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import kotlin.random.Random

class ThreadRepositoryServerImpl @Inject constructor(
    @Named("threadType")
            private val threadType: Byte,

    @Named("threadId")
            private val threadId: Long,

    private val apiService: ApiService,
    private val db: AppDb,
    private val threadDao: ThreadDao,
    private val postDao: PostDao,
    private val postAttachmentDao: PostAttachmentDao,
    private val appAuth: AppAuth,
    private val postRepository: PostRepository,
    private val networkStatus: NetworkStatus
) : ThreadRepository {
    override val lastUpdateTime: MutableLiveData<Long> = MutableLiveData(0L)

    override val threadStatus: MutableLiveData<ThreadStatus> = MutableLiveData(ThreadStatus())

    //private lateinit var updaterJob: Job

    @Inject
    lateinit var mediatorFactory: PostRemoteMediatorFactory


   override val thread: Flow<PostsThread> = threadDao.get(threadType, threadId)

   override fun posts(refreshTarget: ThreadLoadTarget): Flow<PagingData<Post>> =
        @OptIn(ExperimentalPagingApi::class)
        Pager(
            config = PagingConfig(pageSize = 20),
            remoteMediator = mediatorFactory.create(threadType, threadId, refreshTarget),
            pagingSourceFactory = {
                postDao.freshPosts(threadType, threadId)
            },
        ).flow.map { pagingData ->
            pagingData.map {
                it.toDto()
            }
        }


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

                body.data.draftAttachments?.let {
                    db.withTransaction {
                        postAttachmentDao.removeUploaded(threadType, threadId)
                        postAttachmentDao.insert(
                            it.toEntity().map {
                                it.copy(status = PostAttachment.STATUS_UPLOADED)
                            }
                        )
                    }
                } ?: run {
                    postAttachmentDao.removeUploaded(threadType, threadId)
                }

                networkStatus.setStatus(NetworkStatus.Status.ONLINE)
            } catch (e: IOException) {
                networkStatus.setStatus(NetworkStatus.Status.ERROR)
            } catch (e: Exception) {

            }
        }
    }

    override suspend fun changeSubscription(newStatus: Byte) {
        appAuth.myToken()?.let { myToken ->
            try {
                val response = apiService.changeSubscription(
                    threadType = threadType,
                    threadId = threadId,
                    status = newStatus
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

    override suspend fun checkThreadUpdates()  {
        while (true) {
            delay(2_000L)
            println("CheckThreadUpdates for thread " + threadId + " : " + Random.nextInt(0, 100))
            continue
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
                    if (body.data.time > threadStatus.value!!.lastUpdateTime) {
                        val newStatus = ThreadStatus(
                            lastUpdateTime = body.data.time,
                            lastMessageTime = body.data.time // TODO надо получать время последнего сообщения
                        )
                        threadStatus.postValue(newStatus)
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

//    override fun startCheckUpdates() {
//        updaterJob = CoroutineScope(Dispatchers.Default).launch {
//            checkForUpdates()
//        }
//    }
//
//    override fun stopCheckUpdates() {
//        updaterJob.cancel()
//    }
}


/**
 * Класс, описывающий задачу для загрузки чата, начиная с определенного поста
 * Передается в RemoteMediator чтобы загрузить посты от указанного в настройках
 * А также используется во фрагменте, чтобы после загрузки отобразить соответствующий пост
 */
class ThreadLoadTarget(val targetPostId: Long) {
    companion object {
        const val TARGET_POSITION_FIRST = -1L // Первое сообщение
        const val TARGET_POSITION_LAST = -2L // Последнее сообщение
        const val TARGET_POSITION_FIRST_UNREAD = -3L // Первое непрочитанное сообщение
    }

    fun targetApiParameter():String {
        return when (targetPostId) {
            TARGET_POSITION_LAST -> "last"
            TARGET_POSITION_FIRST -> "first"
            TARGET_POSITION_FIRST_UNREAD -> "first_unread"
            else -> targetPostId.toString()
        }
    }
}