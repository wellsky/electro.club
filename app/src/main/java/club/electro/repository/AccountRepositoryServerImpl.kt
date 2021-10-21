package club.electro.repository

import android.app.Application
import club.electro.R
import club.electro.api.ApiService
import club.electro.auth.AppAuth
import club.electro.dao.AreaDao
import club.electro.dao.PostDao
import club.electro.error.ApiError
import javax.inject.Inject

class AccountRepositoryServerImpl @Inject constructor(
        private val application: Application,
        private val postDao: PostDao,
        private val areaDao: AreaDao,
        private val apiService: ApiService
    ): AccountRepository {
    //private val postDao: PostDao = AppDb.getInstance(context = application).postDao()
    //private val areaDao: AreaDao = AppDb.getInstance(context = application).areaDao()

    override suspend fun signIn(login: String, password: String): Boolean {
        val params = HashMap<String?, String?>()
        params["access_token"] = application.getString(R.string.electro_club_access_token)
        params["method"] = "login"
        params["email"] = login
        params["password"] = password

        val response = apiService.signIn(params)
        val body = response.body() ?: throw ApiError(response.code(), response.message())
        body.data.user?.let {
            AppAuth.getInstance().setAuth(it.user_id, it.user_token, it.nickname,it.thumbnail)
            return true
        }
        return false
    }

    override suspend fun signOut() {
        postDao.clear()
        areaDao.clear()
    }
}