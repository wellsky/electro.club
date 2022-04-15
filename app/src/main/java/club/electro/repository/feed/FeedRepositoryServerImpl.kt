package club.electro.repository.feed

import club.electro.api.ApiService
import club.electro.api.checkIfOk
import club.electro.dao.FeedPostDao
import club.electro.dto.FeedPost
import club.electro.entity.toEntity
import club.electro.error.ApiError
import club.electro.error.UnknownError
import club.electro.model.NetworkStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepositoryServerImpl @Inject constructor(
    private val dao : FeedPostDao,
    private val apiService: ApiService,
    private val networkStatus : NetworkStatus
): FeedRepository {


    override var data: Flow<List<FeedPost>> = dao.flowFeedByPublshedDESC().map {
        it.map {
            val post = it.toDto()
            post
        }
    }.flowOn(Dispatchers.Default)

    override suspend fun getFeedPosts() {
        val result = apiService.getFeedPosts().checkIfOk(networkStatus)

        if (result != null) {
            dao.insert(result.data.messages.toEntity())
        }
    }
}