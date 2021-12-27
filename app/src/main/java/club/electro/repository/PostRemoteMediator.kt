package club.electro.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import club.electro.R
import club.electro.di.DependencyContainer
import club.electro.entity.PostEntity
import club.electro.entity.PostRemoteKeyEntity
import club.electro.entity.toEntity
import club.electro.error.ApiError
import club.electro.model.NetworkStatus
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    val diContainer: DependencyContainer,
    val threadType: Byte,
    val threadId: Long,
    val target: ThreadLoadTarget
) : RemoteMediator<Int, PostEntity>() {
    val resources = diContainer.resources
    val apiService = diContainer.apiService
    val repository = diContainer.postRepository
    val networkStatus = diContainer.networkStatus
    val appAuth = diContainer.appAuth
    val db = diContainer.appDb
    val postDao = db.postDao()
    val postRemoteKeyDao = db.postRemoteKeyDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {

                    // Если надо начать от последнего сообщения, то при рефреше загружаем отрицательное число постов (т.е. вверх по списку)
                    var count =
                        if (target.targetPostId == ThreadLoadTarget.TARGET_POSITION_LAST)
                            -state.config.pageSize else state.config.pageSize


                    // Если anchorPosition не null, значит был вызван adapter.refresh() (особенности библиотеки) и надо обновить видимые посты
                    val from = state.anchorPosition?.let {
                        println(
                            "anchor: " + state.anchorPosition + " postId: " + state.closestItemToPosition(
                                it
                            )?.id + " gravity: " + target.targetPostId
                        )

                        // В зависимости от "гравитации" ближайший видимый пост будет либо выше, либо ниже видимой области экрана
                        // Значит загрузку надо начать с сообщений либо до либо после этого поста
                        count =
                            if (target.targetPostId == ThreadLoadTarget.TARGET_POSITION_LAST)
                                state.config.pageSize else -state.config.pageSize

                        state.closestItemToPosition(it)?.id.toString()
                    }
                        ?: target.targetApiParameter() // Это не adapter.refresh(), начинаем загрузку с указанного в цели значения

                    println("REFRESH FROM " + from + " " + count + " anchor: " + state.anchorPosition)

                    apiService.getThreadPosts(
                        threadType = threadType,
                        threadId = threadId,
                        from = from,
                        included = 1, //Включая указанный в from пост
                        count = count,
                    )
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