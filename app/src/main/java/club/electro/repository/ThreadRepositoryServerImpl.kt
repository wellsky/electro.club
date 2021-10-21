package club.electro.repository

import android.app.Application
import club.electro.R
import club.electro.api.ApiService
import club.electro.auth.AppAuth
import club.electro.dao.PostDao
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
import javax.inject.Inject

// TODO - убрать val перед aaplication, когда getString() уже не понадобится
class ThreadRepositoryServerImpl @Inject constructor (
            //private val threadId: Long,
            private val application: Application,
            private val dao: PostDao,
            private val appAuth: AppAuth,
            private val apiService: ApiService
        ) : ThreadRepository {
    //private val dao: PostDao = AppDb.getInstance(context = application).postDao()

    // override var data: Flow<List<Post>> = dao.getAll(threadId).map(List<PostEntity>::toDto).flowOn(Dispatchers.Default)
    override lateinit var data: Flow<List<Post>>

    //val appAuth = AppAuth.getInstance()

    override suspend fun getThreadPosts(threadId: Long) {
        try {
            val params = HashMap<String?, String?>()
            params["access_token"] = application.getString(R.string.electro_club_access_token)
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
            data = dao.getAll(threadId).map(List<PostEntity>::toDto).flowOn(Dispatchers.Default)
        } catch (e: IOException) {
            //println(e.message.toString())
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}