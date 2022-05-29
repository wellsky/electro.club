package club.electro.repository.thread

import androidx.lifecycle.MutableLiveData
import androidx.paging.*
import androidx.room.withTransaction
import club.electro.api.ApiService
import club.electro.api.NetworkService
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
    private val networkService: NetworkService,
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
        networkService.safeApiCall(
            apiCall = {
                apiService.getThread(
                    threadType = threadType,
                    threadId = threadId
                )
            },
            onSuccess = {
                threadDao.insert(it.data.thread.toEntity())

                it.data.draftAttachments?.let {
                    db.withTransaction {
                        postAttachmentDao.removeUploadedDrafts(threadType, threadId)
                        postAttachmentDao.insert(
                            it.toEntity().map {
                                it.copy(status = PostAttachment.STATUS_UPLOADED)
                            }
                        )
                    }
                } ?: run {
                    postAttachmentDao.removeUploadedDrafts(threadType, threadId)
                }
            }
        )

//        try {
//            val response = apiService.getThread(
//                threadType = threadType,
//                threadId = threadId
//            )
//
//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
//            val body = response.body() ?: throw ApiError(response.code(), response.message())
//
//            threadDao.insert(body.data.thread.toEntity())
//
//            body.data.draftAttachments?.let {
//                db.withTransaction {
//                    postAttachmentDao.removeUploadedDrafts(threadType, threadId)
//                    postAttachmentDao.insert(
//                        it.toEntity().map {
//                            it.copy(status = PostAttachment.STATUS_UPLOADED)
//                        }
//                    )
//                }
//            } ?: run {
//                postAttachmentDao.removeUploadedDrafts(threadType, threadId)
//            }
//
//            networkStatus.setStatus(NetworkStatus.Status.ONLINE)
//        } catch (e: IOException) {
//            networkStatus.setStatus(NetworkStatus.Status.ERROR)
//        } catch (e: Exception) {
//
//        }
    }


    override suspend fun setThreadVisit() {
        networkService.safeApiCall(
            apiCall = {
                apiService.setThreadVisit(
                    threadType = threadType,
                    threadId = threadId
                )
            },
        )
//        try {
//            val response = apiService.setThreadVisit(
//                threadType = threadType,
//                threadId = threadId
//            )
//
//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
//            //val body = response.body() ?: throw ApiError(response.code(), response.message())
//            networkStatus.setStatus(NetworkStatus.Status.ONLINE)
//        } catch (e: IOException) {
//            networkStatus.setStatus(NetworkStatus.Status.ERROR)
//        } catch (e: Exception) {
//
//        }
    }

    override suspend fun changeSubscription(newStatus: Byte) {
        if (!appAuth.authorized()) return

        networkService.safeApiCall(
            apiCall = {
                apiService.changeSubscription(
                    threadType = threadType,
                    threadId = threadId,
                    status = newStatus
                )
            },
            onSuccess = {
                threadDao.insert(it.data.thread.toEntity())
            }
        )
//        try {
//            val response = apiService.changeSubscription(
//                threadType = threadType,
//                threadId = threadId,
//                status = newStatus
//            )
//
//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
//            val body = response.body() ?: throw ApiError(response.code(), response.message())
//            threadDao.insert(body.data.thread.toEntity())
//            networkStatus.setStatus(NetworkStatus.Status.ONLINE)
//        } catch (e: IOException) {
//            networkStatus.setStatus(NetworkStatus.Status.ERROR)
//        } catch (e: Exception) {
//
//        }
    }

    override suspend fun savePostToServer(post: Post) {
        if (post.id == 0L) {
            postAttachmentDao.removeUploadedDrafts(threadType, threadId)
        }
        postRepository.savePostToServer(post.copy(
            threadId = threadId,
            threadType = threadType,
        ))
    }

    override suspend fun removePost(post: Post) {
        postRepository.removePostFromServer(post)
    }

    override suspend fun checkThreadUpdates() {
        while (true) {
            delay(2_000L)

            networkService.safeApiCall(
                apiCall = {
                    apiService.getAreaStatus(
                        type = threadType,
                        objectId = threadId
                    )
                },
                onSuccess = {
                    with (it.data) {
                        if (lastUpdateTime > threadStatus.value!!.lastUpdateTime) {
                            val newStatus = ThreadStatus(
                                lastUpdateTime = lastUpdateTime,
                                lastMessageTime = lastMessageTime,
                                messagesCount = messagesCount// TODO надо получать время последнего сообщения
                            )
                            threadStatus.postValue(newStatus)
                        }
                    }
                }
            )

//            try {
//                val response = apiService.getAreaStatus(
//                    type = threadType,
//                    objectId = threadId
//                )
//                if (!response.isSuccessful) {
//                    throw ApiError(response.code(), response.message())
//                }
//
//                val body = response.body() //?: throw ApiError(response.code(), response.message())
//
//                body?.let {
//                    with (it.data) {
//                        if (lastUpdateTime > threadStatus.value!!.lastUpdateTime) {
//                            val newStatus = ThreadStatus(
//                                lastUpdateTime = lastUpdateTime,
//                                lastMessageTime = lastMessageTime,
//                                messagesCount = messagesCount// TODO надо получать время последнего сообщения
//                            )
//                            threadStatus.postValue(newStatus)
//                        }
//                    }
//                    networkStatus.setStatus(NetworkStatus.Status.ONLINE)
//                }
//            }  catch (e: IOException) {
//                networkStatus.setStatus(NetworkStatus.Status.ERROR)
//            } catch (e: Exception) {
//                // throw UnknownError
//            }
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