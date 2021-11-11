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
) : RemoteMediator<Int, PostEntity>() {
    val resources = diContainer.resources
    val apiService = diContainer.apiService
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
                    println("FROM last -" + state.config.pageSize)
                    apiService.getThreadPosts(
                        access_token = resources.getString(R.string.electro_club_access_token),
                        user_token = appAuth.myToken(),
                        threadType = threadType,
                        threadId = threadId,
                        from = "last",
                        count = -state.config.pageSize
                    )
                }
                LoadType.PREPEND -> {
//                    val item = state.firstItemOrNull() ?: return MediatorResult.Success(
//                        endOfPaginationReached = false
//                    )
                    val id = postRemoteKeyDao.max(threadType, threadId) ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    println("FROM " + id + " " + state.config.pageSize)
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
                    println("FROM " + id + " -" + state.config.pageSize)
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
                                    postId = body.data.messages.first().id,
                                ),
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.BEFORE,
                                    threadType = threadType,
                                    threadId = threadId,
                                    postId = body.data.messages.last().id,
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
                                postId = body.data.messages.first().id,
                            //)
                        )
                    }
                    LoadType.APPEND -> {
                        postRemoteKeyDao.update(
                            //PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.BEFORE,
                                threadType = threadType,
                                threadId = threadId,
                                postId = body.data.messages.last().id,
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