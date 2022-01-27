package club.electro.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import club.electro.api.ApiService
import club.electro.dao.PostDao
import club.electro.dao.PostRemoteKeyDao
import club.electro.db.AppDb
import club.electro.entity.PostEntity
import club.electro.entity.PostRemoteKeyEntity
import club.electro.entity.toEntity
import club.electro.error.ApiError
import club.electro.model.NetworkStatus
import club.electro.repository.post.PostRepository
import club.electro.repository.thread.ThreadLoadTarget
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.io.IOException

@AssistedFactory
interface PostRemoteMediatorFactory {
    fun create(threadType: Byte, threadId: Long, target: ThreadLoadTarget): PostRemoteMediator
}

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator @AssistedInject constructor(
    @Assisted
    val threadType: Byte,
    @Assisted
    val threadId: Long,
    @Assisted
    val target: ThreadLoadTarget,

    private val apiService: ApiService,
    private val db: AppDb,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val repository: PostRepository,
    private val networkStatus: NetworkStatus,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {

                    // Если anchorPosition не null, значит был вызван adapter.refresh() (особенности библиотеки) и надо обновить видимые посты
                    state.anchorPosition?.let {
                        val from = state.closestItemToPosition(it)?.id.toString()

                        println("REFRESH FROM " + from + " NEAREST=" + state.config.pageSize + " anchor: " + state.anchorPosition)

                        apiService.getThreadPosts(
                            threadType = threadType,
                            threadId = threadId,
                            from = from,
                            nearest = state.config.pageSize / 2,
                        )
                    } ?: run {
                        // Это не adapter.refresh(), начинаем загрузку с указанного в цели значения
                        val from = target.targetApiParameter()

                        // Если надо начать от последнего сообщения, то при рефреше загружаем отрицательное число постов (т.е. вверх по списку)
                        var count = if (target.targetPostId == ThreadLoadTarget.TARGET_POSITION_LAST) -state.config.pageSize else state.config.pageSize

                        println("REFRESH FROM " + from + " COUNT=" + count + " anchor: " + state.anchorPosition)

                        apiService.getThreadPosts(
                            threadType = threadType,
                            threadId = threadId,
                            from = from,
                            included = 1, //Включая указанный в from пост
                            count = count,
                        )
                    }
                }

                LoadType.PREPEND -> {
                    val id =
                        postRemoteKeyDao.max(threadType, threadId) ?: return MediatorResult.Success(
                            endOfPaginationReached = false
                        )
                    println("PREPEND FROM " + id + " " + state.config.pageSize)
                    apiService.getThreadPosts(
                        threadType = threadType,
                        threadId = threadId,
                        from = id.toString(),
                        count = state.config.pageSize
                    )
                }
                LoadType.APPEND -> {
                    val id =
                        postRemoteKeyDao.min(threadType, threadId) ?: return MediatorResult.Success(
                            endOfPaginationReached = false
                        )
                    println("APPEND FROM " + id + " -" + state.config.pageSize)

                    apiService.getThreadPosts(
                        threadType = threadType,
                        threadId = threadId,
                        from = id.toString(),
                        count = -state.config.pageSize
                    )
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message(),
            )

            db.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        postRemoteKeyDao.removeThread(threadType, threadId)
                        postRemoteKeyDao.insert(
                            listOf(
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.AFTER,
                                    threadType = threadType,
                                    threadId = threadId,
                                    postId = body.data.messages.last().id,
                                ),
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.BEFORE,
                                    threadType = threadType,
                                    threadId = threadId,
                                    postId = body.data.messages.first().id,
                                ),
                            )
                        )
                        postDao.unfreshThread(threadType = threadType, threadId = threadId)
                    }
                    LoadType.PREPEND -> {
                        postRemoteKeyDao.update(
                            type = PostRemoteKeyEntity.KeyType.AFTER,
                            threadType = threadType,
                            threadId = threadId,
                            postId = body.data.messages.last().id,
                        )
                    }
                    LoadType.APPEND -> {
                        postRemoteKeyDao.update(
                            type = PostRemoteKeyEntity.KeyType.BEFORE,
                            threadType = threadType,
                            threadId = threadId,
                            postId = body.data.messages.first().id,
                        )
                    }
                }

                val freshEntities = body.data.messages.map {
                    it.toEntity().copy(
                        fresh = true
                    )
                }

                repository.prepareAndSaveLocal(freshEntities)
            }

            networkStatus.setStatus(NetworkStatus.Status.ONLINE)
            return MediatorResult.Success(endOfPaginationReached = body.data.messages.isEmpty())
        } catch (e: IOException) {
            networkStatus.setStatus(NetworkStatus.Status.ERROR)
            return MediatorResult.Error(e)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}