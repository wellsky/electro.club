import android.app.Application
import club.electro.R
import club.electro.api.Api
import club.electro.auth.AppAuth
import club.electro.dao.AreaDao
import club.electro.dao.PostDao
import club.electro.db.AppDb
import club.electro.di.DependencyContainer
import club.electro.error.ApiError
import club.electro.repository.AccountRepository

class AccountRepositoryServerImpl(diContainer: DependencyContainer): AccountRepository {
    private val postDao = diContainer.appDb.postDao()
    private val areaDao = diContainer.appDb.areaDao()
    private val resources = diContainer.context.resources
    private val apiService = diContainer.apiService
    private val appAuth = diContainer.appAuth

    override suspend fun signIn(login: String, password: String): Boolean {
        val params = HashMap<String?, String?>()
        params["access_token"] = resources.getString(R.string.electro_club_access_token)
        params["method"] = "login"
        params["email"] = login
        params["password"] = password

        val response = apiService.signIn(params)
        val body = response.body() ?: throw ApiError(response.code(), response.message())
        body.data.user?.let {
            appAuth.setAuth(it.user_id, it.user_token, it.nickname,it.thumbnail)
            return true
        }
        return false
    }

    override suspend fun signOut() {
        postDao.clearAll()
        areaDao.clearAll()
    }
}