package club.electro.repository.user

import club.electro.api.ApiService
import club.electro.dao.UserDao
import club.electro.dto.*
import club.electro.entity.toEntity
import club.electro.error.ApiError
import club.electro.error.NetworkError
import club.electro.error.UnknownError
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import javax.inject.Inject

class UserRepositoryServerImpl @Inject constructor(
    private val apiService: ApiService,
    private val dao: UserDao
) : UserRepository {

    override suspend fun getLocalById(id: Long, onLoadedCallback:  (suspend () -> Unit)?): User? {
        return dao.getById(id)?.let {
            if (it.name.isNotBlank()) {
                it.toDto()
            } else {
                null
            }
        } ?: onLoadedCallback?.run {
            dao.insert(User(
                id = id,
                name = "",
                created = 0,
                lastVisit = 0,
            ).toEntity())

            CoroutineScope(Dispatchers.Default).launch {
                val user = getRemoteById(id)
                dao.insert(user.toEntity())
                onLoadedCallback()
            }
            null
        }
    }

    // TODO по сути выполняет то же самое, что getLocalById, но через Flow, а не через callback
    // Но создает фейкового пользователя, чтобы предотвратить запросы к серверу из других тэгов о том же пользователе
    override fun getUserProfile(id: Long): Flow<User> = flow {
        dao.getById(id)?.let {
            emit(it.toDto())
        }
        val user = getRemoteById(id)
        dao.insert(user.toEntity())
        emit(user)
    }.flowOn(Dispatchers.Default)


    override suspend fun getRemoteById(id: Long): User {
        try {
            val response = apiService.getUserProfile(
                userId = id
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            return body.data.user
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}