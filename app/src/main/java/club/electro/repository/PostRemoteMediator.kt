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

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    val diContainer: DependencyContainer,
    val threadType: Byte,
    val threadId: Long,
    val target: ThreadTargetPost
) : RemoteMediator<Int, PostEntity>() {
    val resources = diContainer.resources
    val apiService = diContainer.apiService
    val appAuth = diContainer.appAuth
    val db = diContainer.appDb
    val postDao = db.postDao()
    val postRemoteKeyDao = db.postRemoteKeyDao()

    /**
     * Две задачи, которые надо решить:
     * 1. Обращаться к Api постоянно во время скроллинга, чтобы обновить посты в БД.
     * При этом не удалять заранее все посты, тода можно быстро будет скроллить в середину чата, и только после отображения обновлять.
     *
     * 2. Скроллить по id поста.
     * Как вариант - загружать данные с определенной позиции.
     */

    init {
        println("Mediator init")
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    println("REFRESH FROM " + target.targetPostPosition + " -" + state.config.pageSize)
                    apiService.getThreadPosts(
                        access_token = resources.getString(R.string.electro_club_access_token),
                        user_token = appAuth.myToken(),
                        threadType = threadType,
                        threadId = threadId,
                        from = target.targetApiParameter(),
                        count = -state.config.pageSize
                    )
                }
                // TODO поменять логику PREPEND и APPEND чтобы соответствовала значению слов?
                LoadType.PREPEND -> {
//                    val item = state.firstItemOrNull() ?: return MediatorResult.Success(
//                        endOfPaginationReached = false
//                    )
                    val id = postRemoteKeyDao.max(threadType, threadId) ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    println("PREPEND FROM " + id + " " + state.config.pageSize)
                    apiService.getThreadPosts(
                        access_token = resources.getString(R.string.electro_club_access_token),
                        user_token = appAuth.myToken(),
                        threadType = threadType,
                        threadId = threadId,
                        from = id.toString(),
                        count = state.config.pageSize
                    )
                }
                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min(threadType, threadId) ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    println("APPEND FROM " + id + " -" + state.config.pageSize)
                    apiService.getThreadPosts(
                        access_token = resources.getString(R.string.electro_club_access_token),
                        user_token = appAuth.myToken(),
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

            postDao.insert(body.data.messages.toEntity())

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
                        postDao.removeThread(threadType = threadType, threadId = threadId)
                    }
                    LoadType.PREPEND -> {
                        postRemoteKeyDao.update(
                            //PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.AFTER,
                                threadType = threadType,
                                threadId = threadId,
                                postId = body.data.messages.last().id,
                            //)
                        )
                    }
                    LoadType.APPEND -> {
                        postRemoteKeyDao.update(
                            //PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.BEFORE,
                                threadType = threadType,
                                threadId = threadId,
                                postId = body.data.messages.first().id,
                            //)
                        )
                    }
                }
                postDao.insert(body.data.messages.toEntity())
            }


            return MediatorResult.Success(endOfPaginationReached = body.data.messages.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}