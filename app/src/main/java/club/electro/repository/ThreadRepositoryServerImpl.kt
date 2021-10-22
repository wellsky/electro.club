package club.electro.repository

import android.app.Application
import club.electro.R
import club.electro.api.Api
import club.electro.auth.AppAuth
import club.electro.dao.PostDao
import club.electro.db.AppDb
import club.electro.di.DependencyContainer
import club.electro.dto.Post
import club.electro.entity.PostEntity
import club.electro.entity.toDto
import club.electro.entity.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import club.electro.error.*
import kotlinx.coroutines.flow.flowOn

// TODO - убрать val перед aaplication, когда getString() уже не понадобится
class ThreadRepositoryServerImpl(
            diContainer: DependencyContainer,
            val threadId: Long
        ) : ThreadRepository {

    private val dao = diContainer.appDb.postDao()
    private val resources = diContainer.context.resources
    private val apiService = diContainer.apiService

    override var data: Flow<List<Post>> = dao.getAll(threadId).map(List<PostEntity>::toDto).flowOn(Dispatchers.Default)

    val appAuth = AppAuth.getInstance()

    override suspend fun getThreadPosts() {
        try {
            val params = HashMap<String?, String?>()
            params["access_token"] = resources.getString(R.string.electro_club_access_token)
            params["user_token"] = appAuth.myToken()
            params["method"] = "getPosts"
            params["threadType"] = "1"
            params["threadId"] = threadId.toString()

            val response = apiService.getThreadPosts(params)
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