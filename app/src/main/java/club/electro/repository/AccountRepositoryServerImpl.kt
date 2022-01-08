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