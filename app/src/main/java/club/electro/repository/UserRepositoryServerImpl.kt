package club.electro.repository

import androidx.lifecycle.MutableLiveData
import club.electro.R
import club.electro.di.DependencyContainer
import club.electro.dto.*
import club.electro.entity.MapMarkerEntity
import club.electro.entity.UserEntity
import club.electro.entity.toDto
import club.electro.entity.toEntity
import club.electro.error.ApiError
import club.electro.error.NetworkError
import club.electro.error.UnknownError
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException

class UserRepositoryServerImpl(
    private val diContainer: DependencyContainer,
) : UserRepository {
    private val apiService = diContainer.apiService
    private val dao = diContainer.appDb.userDao()

//    var currentProfileFlow = MutableStateFlow(0L)

//    override val currentProfile: Flow<User> = currentProfileFlow.flatMapLatest {
//        dao.flowById(it).map {
//            it?.toDto() ?: EmptyUserProfile()
//        }.flowOn(Dispatchers.Default)
//    }

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
            ).toEntity())

            CoroutineScope(Dispatchers.Default).launch {
                val user = getRemoteById(id)
                dao.insert(user.toEntity())
                onLoadedCallback()
            }
            null
        }
    }


    override fun getUserProfile(id: Long): Flow<User> = flow {
        dao.getById(id)?.let {
            emit(it.toDto())
        }
        val user = getRemoteById(id)
        dao.insert(user.toEntity())
        emit(user)
    }.flowOn(Dispatchers.Default)

//    override suspend fun setCurrentProfile(id: Long) {
//        currentProfileFlow.value = id
//        val user = getRemoteById(id)
//        dao.insert(user.toEntity())
//    }

    override suspend fun getRemoteById(id: Long): User {
        try {
            val response = apiService.getUserProfile(
                userId = id
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            return body.data.user.toDto()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}