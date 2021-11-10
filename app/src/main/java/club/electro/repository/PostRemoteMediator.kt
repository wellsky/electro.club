package club.electro.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import club.electro.R
import club.electro.di.DependencyContainer
import club.electro.entity.PostEntity
import club.electro.error.ApiError


@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    val diContainer: DependencyContainer,
    val threadType: Byte,
    val threadId: Long,
) : RemoteMediator<Int, PostEntity>() {
    val apiService = diContainer.apiService
    val appAuth = diContainer.appAuth
    val db = diContainer.appDb
    val postDao = db.postDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        val resources = diContainer.resources
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> apiService.getThreadPosts(
                    access_token = resources.getString(R.string.electro_club_access_token),
                    user_token = appAuth.myToken(),
                    threadType = threadType,
                    threadId = threadId
                )
                LoadType.PREPEND -> {
                    val item = state.firstItemOrNull() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    apiService.getThreadPosts(
                        access_token = resources.getString(R.string.electro_club_access_token),
                        user_token = appAuth.myToken(),
                        threadType = threadType,
                        threadId = threadId
                    )
                }
                LoadType.APPEND -> {
                    val item = state.lastItemOrNull() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    apiService.getThreadPosts(
                        access_token = resources.getString(R.string.electro_club_access_token),
                        user_token = appAuth.myToken(),
                        threadType = threadType,
                        threadId = threadId
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

//            postDao.insert(body.toEntity())

//            db.withTransaction {
//                when (loadType) {
//                    LoadType.REFRESH -> {
//                        //postRemoteKeyDao.removeAll()
//                        postRemoteKeyDao.insert(
//                            listOf(
//                                PostRemoteKeyEntity(
//                                    type = PostRemoteKeyEntity.KeyType.AFTER,
//                                    id = body.first().id,
//                                ),
//                                PostRemoteKeyEntity(
//                                    type = PostRemoteKeyEntity.KeyType.BEFORE,
//                                    id = body.last().id,
//                                ),
//                            )
//                        )
//                        //postDao.removeAll()
//                    }
//                    LoadType.PREPEND -> {
//                        postRemoteKeyDao.insert(
//                            PostRemoteKeyEntity(
//                                type = PostRemoteKeyEntity.KeyType.AFTER,
//                                id = body.first().id,
//                            )
//                        )
//                    }
//                    LoadType.APPEND -> {
//                        postRemoteKeyDao.insert(
//                            PostRemoteKeyEntity(
//                                type = PostRemoteKeyEntity.KeyType.BEFORE,
//                                id = body.last().id,
//                            )
//                        )
//                    }
//                }
//                postDao.insert(body.toEntity())
//            }


//            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
            return MediatorResult.Success(endOfPaginationReached = false)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}