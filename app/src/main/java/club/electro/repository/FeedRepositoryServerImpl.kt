package club.electro.repository

import club.electro.R
import club.electro.api.ApiService
import club.electro.auth.AppAuth
import club.electro.dao.FeedPostDao
import club.electro.di.DependencyContainer
import club.electro.dto.FeedPost
import club.electro.entity.toEntity
import club.electro.error.ApiError
import club.electro.error.NetworkError
import club.electro.error.UnknownError
import club.electro.model.NetworkStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepositoryServerImpl(): FeedRepository {
    @Inject
    lateinit var dao : FeedPostDao
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var appAuth : AppAuth
    @Inject
    lateinit var networkStatus : NetworkStatus

    override var data: Flow<List<FeedPost>> = dao.flowFeedByPublshedDESC().map {
        it.map {
            val post = it.toDto()
            post
        }
    }.flowOn(Dispatchers.Default)

    override suspend fun getFeedPosts() {
        try {
            val response = apiService.getFeedPosts()

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            dao.insert(body.data.messages.toEntity())
            networkStatus.setStatus(NetworkStatus.Status.ONLINE)
        } catch (e: IOException) {
            networkStatus.setStatus(NetworkStatus.Status.ERROR)
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}