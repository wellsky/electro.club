package club.electro.repository

import club.electro.api.ApiService
import club.electro.auth.AppAuth
import club.electro.dao.AreaDao
import club.electro.dao.PostDao
import club.electro.di.DependencyContainer
import club.electro.error.ApiError
import club.electro.repository.AccountRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryServerImpl @Inject constructor(
    private val apiService: ApiService,
): AccountRepository {
    @Inject
    lateinit var postDao : PostDao
    @Inject
    lateinit var areaDao : AreaDao
    @Inject
    lateinit var appAuth : AppAuth

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