package club.electro.repository

import androidx.lifecycle.MutableLiveData
import club.electro.R
import club.electro.di.DependencyContainer
import club.electro.dto.EmptyUserProfile
import club.electro.dto.MapMarker
import club.electro.dto.User
import club.electro.entity.MapMarkerEntity
import club.electro.entity.UserEntity
import club.electro.entity.toDto
import club.electro.entity.toEntity
import club.electro.error.ApiError
import club.electro.error.NetworkError
import club.electro.error.UnknownError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserRepositoryServerImpl(
    private val diContainer: DependencyContainer,
    private val userId: Long
) : UserRepository {
    private val resources = diContainer.context.resources
    private val appAuth = diContainer.appAuth
    private val apiService = diContainer.apiService
    private val dao = diContainer.appDb.userDao()

    //override val currentProfile: Flow<User> = dao.getById(12343545).map(UserEntity::toDto).flowOn(Dispatchers.Default)
    override val currentProfile: Flow<User> = dao.getById(userId).map {
        it?.toDto() ?: EmptyUserProfile()
    }.flowOn(Dispatchers.Default)

    override suspend fun getUserProfile(id: Long) {
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
            dao.insert(body.data.user.toDto().toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}