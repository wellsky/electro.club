package club.electro.repository.feed

import club.electro.api.ApiService
import club.electro.api.NetworkService
import club.electro.dao.FeedPostDao
import club.electro.dto.FeedPost
import club.electro.entity.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepositoryServerImpl @Inject constructor(
    private val dao : FeedPostDao,
    private val apiService: ApiService,
    private val networkService: NetworkService
): FeedRepository {


    override var data: Flow<List<FeedPost>> = dao.flowFeedByPublshedDESC().map {
        it.map {
            val post = it.toDto()
            post
        }
    }.flowOn(Dispatchers.Default)

    override suspend fun getFeedPosts() {
        networkService.safeApiCall(
            apiCall = {
                apiService.getFeedPosts()
            },
            onSuccess = {
                dao.insert(it.data.messages.toEntity())
            }
        )
    }
}