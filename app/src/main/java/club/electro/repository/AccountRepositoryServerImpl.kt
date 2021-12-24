import club.electro.di.DependencyContainer
import club.electro.error.ApiError
import club.electro.repository.AccountRepository

class AccountRepositoryServerImpl(diContainer: DependencyContainer): AccountRepository {
    private val postDao = diContainer.appDb.postDao()
    private val areaDao = diContainer.appDb.areaDao()
    private val apiService = diContainer.apiService
    private val appAuth = diContainer.appAuth

    override suspend fun signIn(login: String, password: String): Boolean {
        val response = apiService.signIn(
            email = login,
            password = password
        )
        val body = response.body() ?: throw ApiError(response.code(), response.message())
        body.data.user?.let {
            appAuth.setAuth(it.user_id, it.user_token, it.nickname,it.thumbnail, it.transport_name, it.transport_image)
            return true
        }
        return false
    }

    override suspend fun signOut() {
        postDao.removeAll()
        areaDao.removeAll()
    }
}