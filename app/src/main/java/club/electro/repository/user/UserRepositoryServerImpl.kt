package club.electro.repository.user

import club.electro.api.ApiService
import club.electro.auth.AppAuth
import club.electro.dao.UserDao
import club.electro.dto.*
import club.electro.entity.toEntity
import club.electro.error.ApiError
import club.electro.error.NetworkError
import club.electro.error.UnknownError
import club.electro.model.NetworkStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import javax.inject.Inject

class UserRepositoryServerImpl @Inject constructor(
    private val apiService: ApiService,
    private val dao: UserDao,
    private val networkStatus: NetworkStatus,
    private val appAuth: AppAuth
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
                getRemoteById(id)?.let {
                    dao.insert(it.toEntity())
                }
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
        getRemoteById(id)?.let {
            dao.insert(it.toEntity())
            emit(it)
        }
    }.flowOn(Dispatchers.Default)


    override suspend fun getRemoteById(id: Long): User? {
        try {
            val response = apiService.getUserProfile(
                userId = id
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            networkStatus.setStatus(NetworkStatus.Status.ONLINE)
            return body.data.user
        } catch (e: IOException) {
            networkStatus.setStatus(NetworkStatus.Status.ERROR)
            return null
            //throw UnknownError
        } catch (e: Exception) {
            return null
            //throw UnknownError
        }
    }

    override suspend fun getChatWith(userId: Long): ThreadLink? {
        if (!appAuth.authorized()) return null
        try {
            val response = apiService.getChatWith(
                userId = userId
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            networkStatus.setStatus(NetworkStatus.Status.ONLINE)
            return body.data
        } catch (e: IOException) {
            networkStatus.setStatus(NetworkStatus.Status.ERROR)
            return null
        } catch (e: Exception) {
            return null
        }
    }
}