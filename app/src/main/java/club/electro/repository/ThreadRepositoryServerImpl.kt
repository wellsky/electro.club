package club.electro.repository

import android.app.Application
import androidx.core.content.res.TypedArrayUtils.getText
import club.electro.R
import club.electro.api.Api
import club.electro.dao.AreaDao
import club.electro.dao.PostDao
import club.electro.dto.Post
import club.electro.dto.SubscriptionArea
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.AreaEntity
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.*
import java.io.IOException
import android.provider.Settings.Global.getString

// TODO - убрать val перед aaplication, когда getString() уже не понадобится
class ThreadRepositoryServerImpl(
            val application: Application,
            val threadId: Long
        ) : ThreadRepository {
    private val dao: PostDao = AppDb.getInstance(context = application).postDao()

    override var data: Flow<List<Post>> = dao.getAll(threadId).map(List<PostEntity>::toDto).flowOn(Dispatchers.Default)

    override suspend fun getThreadPosts() {
        try {
            println("repository loading thread: " + threadId.toString())

            val params = HashMap<String?, String?>()
            println("ddd1")
            params["access_token"] = application.getString(R.string.electro_club_access_token)
            println("ddd2")
            params["user_token"] = application.getString(R.string.temp_user_token)
            println("ddd3")
            params["method"] = "getPosts"
            println("ddd4")
            params["threadType"] = "1"
            println("ddd5")
            params["threadId"] = threadId.toString() // "3940" // 8304 - english, 3940 - russian
            println("ddd6")

            println("start")
            val response = Api.service.getThreadPosts(params)
            println("start1")
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            println(111)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            println(222)
            //this.threadId = threadId
            dao.insert(body.data.messages.toEntity())

            data = dao.getAll(threadId).map(List<PostEntity>::toDto).flowOn(Dispatchers.Default)

            println(333)
        } catch (e: IOException) {
            println(e.message.toString())
            //throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}