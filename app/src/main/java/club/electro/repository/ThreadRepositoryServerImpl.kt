package club.electro.repository

import android.app.Application
import androidx.lifecycle.viewModelScope
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
import java.io.IOException
import club.electro.error.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

// TODO - убрать val перед aaplication, когда getString() уже не понадобится
class ThreadRepositoryServerImpl(
            diContainer: DependencyContainer,
            val threadType: Byte,
            val threadId: Long
        ) : ThreadRepository {

    private val dao = diContainer.appDb.postDao()
    private val resources = diContainer.context.resources
    private val apiService = diContainer.apiService
    private val appAuth = diContainer.appAuth

    override var data: Flow<List<Post>> = dao.flowThreadByPublshedDESC(threadType, threadId).map(List<PostEntity>::toDto).flowOn(Dispatchers.Default)

    private var lastUpdateTime: Long = 0

    private val updaterJob = CoroutineScope(Dispatchers.Default).launch {
        checkForUpdates()
    }

    override suspend fun getThreadPosts() {
        try {
            //println("Loading posts from server")
            val params = HashMap<String?, String?>()
            params["access_token"] = resources.getString(R.string.electro_club_access_token)
            params["user_token"] = appAuth.myToken()
            params["method"] = "getPosts"
            params["thread_type"] = threadType.toString()
            params["thread_id"] = threadId.toString()

            val response = apiService.getThreadPosts(params)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            //clearCurrentPosts()

            val currentMessages = dao.getAllList(threadType, threadId).toDto()

            if (currentMessages.isEmpty()) {
                dao.insert(body.data.messages.toEntity())
            } else {
                val first = currentMessages.first()
                val last = currentMessages.last()

                dao.clearAndInsert(body.data.messages.toEntity(), threadType, threadId, first.published, last.published)
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    // TODO перенести в пагинацию с правильными first и last
    suspend fun clearCurrentPosts() {
        val currentMessages = dao.getAllList(threadType, threadId).toDto()

        println("clear " + threadType + " : " + threadId)
        println(currentMessages)

        val first = currentMessages.first()
        val last = currentMessages.last()

        println(first.published)
        println(last.published)

        dao.clearThreadPeriod(threadType, threadId, first.published, last.published)
    }

    override suspend fun savePost(post: Post) {
        try {
            val params = HashMap<String?, String?>()
            params["access_token"] = resources.getString(R.string.electro_club_access_token)
            params["user_token"] = appAuth.myToken()
            params["method"] = "savePost"
            params["thread_type"] = threadType.toString()
            params["thread_id"] = threadId.toString()
            params["post_id"] = post.id.toString()
            params["post_content"] = post.content

            val response = apiService.savePost(params)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            //dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun checkForUpdates()  {
        while (true) {
            delay(2_000L)

            val params = HashMap<String?, String?>()
            params["access_token"] = resources.getString(R.string.electro_club_access_token)
            params["user_token"] = appAuth.myToken()
            params["method"] = "getAreaModifiedTime"
            params["type"] = threadType.toString()
            params["object_id"] = threadId.toString()

            val response = apiService.getAreaModifiedTime(params)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            val newTime = body.data.time

            if (newTime > lastUpdateTime) {
                if (lastUpdateTime != 0L) getThreadPosts()
                lastUpdateTime = newTime
            }
            //println("lastUpdate: " + lastUpdateTime + ", newTime: " + newTime)
        }
    }

    override fun stop() {
        updaterJob.cancel()
    }
}