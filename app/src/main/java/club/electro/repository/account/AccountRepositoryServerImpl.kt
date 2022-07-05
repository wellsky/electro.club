package club.electro.repository.account

import club.electro.api.ApiService
import club.electro.auth.AppAuth
import club.electro.dao.AreaDao
import club.electro.dao.NotificationDao
import club.electro.dao.PostDao
import club.electro.error.ApiError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryServerImpl @Inject constructor(
    private val apiService: ApiService,
    private val appAuth : AppAuth,
    private val postDao : PostDao,
    private val areaDao : AreaDao,
): AccountRepository {
    override suspend fun signIn(login: String, password: String): Boolean {
        val response = apiService.signIn(
            email = login,
            password = password
        )
        val body = response.body() ?: throw ApiError(response.code(), response.message())
        body.data.user.let {
            appAuth.setAuth(
                id = it.user_id,
                token = it.user_token,
                name = it.nickname,
                avatar = it.thumbnail,
                transportName = it.transport_name,
                transportImage = it.transport_image
            )
            return true
        }
    }

    override suspend fun signOut() {
        postDao.removeAll()
        areaDao.removeAll()
    }
}