package club.electro.repository

import androidx.lifecycle.MutableLiveData
import club.electro.R
import club.electro.di.DependencyContainer
import club.electro.dto.EmptyUserProfile
import club.electro.dto.MapMarker
import club.electro.dto.Post
import club.electro.dto.User
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
    private val resources = diContainer.context.resources
    private val appAuth = diContainer.appAuth
    private val apiService = diContainer.apiService
    private val dao = diContainer.appDb.userDao()

    //override val currentProfile: Flow<User> = dao.getById(12343545).map(UserEntity::toDto).flowOn(Dispatchers.Default)

    var currentProfileFlow = MutableStateFlow(0L)

    override val currentProfile: Flow<User> = currentProfileFlow.flatMapLatest {
        dao.flowById(it).map {
            it?.toDto() ?: EmptyUserProfile()
        }.flowOn(Dispatchers.Default)
    }

    override suspend fun getLocalById(id: Long, onLoadedCallback:  (suspend () -> Unit)?): User? {
        return dao.getById(id)?.let {
            it.toDto()
        } ?: onLoadedCallback?.run {
            dao.insert(User(
                id = id,
                name = "Loading...",
            ).toEntity())

            CoroutineScope(Dispatchers.Default).launch {
                val user = getRemoteById(id)
                dao.insert(user.toEntity())
                //println("call callback " + id)
                onLoadedCallback()
            }
            null
        }
    }

    override suspend fun setCurrentProfile(id: Long) {
        currentProfileFlow.value = id
        val user = getRemoteById(id)
        dao.insert(user.toEntity())
    }

    override suspend fun getRemoteById(id: Long): User {
        println("Load user profile: " + id)
        try {
            val params = HashMap<String?, String?>()
            params["access_token"] = resources.getString(R.string.electro_club_access_token)
            params["user_token"] = appAuth.myToken()
            params["method"] = "getUserProfile"
            params["user_id"] = id.toString()
            val response = apiService.getUserProfile(params)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            //dao.insert(body.data.user.toDto().toEntity())
            return body.data.user.toDto()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}