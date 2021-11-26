package club.electro.repository

import club.electro.R
import club.electro.di.DependencyContainer
import club.electro.dto.FeedPost
import club.electro.entity.toEntity
import club.electro.error.ApiError
import club.electro.error.NetworkError
import club.electro.error.UnknownError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException

class FeedRepositoryServerImpl(diContainer: DependencyContainer): FeedRepository {
    private val dao = diContainer.appDb.feedPostDao()
    private val resources = diContainer.context.resources
    private val apiService = diContainer.apiService
    private val appAuth = diContainer.appAuth
    private val postRepository = diContainer.postRepository

    override var data: Flow<List<FeedPost>> = dao.flowFeedByPublshedDESC().map {
        it.map {
            //println("Preparing post " + it.id)
            val post = it.toDto()
            post
//            val preparedContent: String = PostTextPreparator(post.content)
//                .prepareAll()
//                .get()
//
//            val preparedPost = post.copy(content = preparedContent)
//            preparedPost
        }
    }.flowOn(Dispatchers.Default)

    override suspend fun getFeedPosts() {
        try {
            //println("Loading posts from server")
            val params = HashMap<String?, String?>()
            params["access_token"] = resources.getString(R.string.electro_club_access_token)
            params["method"] = "getFeedPosts"

            appAuth.myToken()?.let {
                params["user_token"] = it
            }

            val response = apiService.getFeedPosts(params)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            dao.insert(body.data.messages.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}