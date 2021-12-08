package club.electro.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import club.electro.R
import club.electro.api.Api
import club.electro.api.ApiService
import club.electro.auth.AppAuth
import club.electro.db.AppDb
import club.electro.repository.PostRepository
import club.electro.repository.PostRepositoryServerImpl
import club.electro.repository.UserRepository
import club.electro.repository.UserRepositoryServerImpl

class DependencyContainer private constructor(val context: Context) {
    val resources = context.resources

    val accessToken = resources.getString(R.string.electro_club_access_token)

    val appDb = AppDb.getInstance(context = context)
    val apiService: ApiService = Api.service
    val appAuth: AppAuth = AppAuth.initApp(context, this)

    val postDao = appDb.postDao()
    val userDao = appDb.userDao()

    val postRepository: PostRepository = PostRepositoryServerImpl(this)
    val userRepository: UserRepository = UserRepositoryServerImpl(this)
    val workManager = WorkManager.getInstance(context)

    init {
        // TODO криво реализованная взаимная инъекция
        postRepository.setupWorkManager(workManager)
    }

    companion object {
        @Volatile
        private var instance: DependencyContainer? = null

        fun getInstance(): DependencyContainer = synchronized(this) {
            instance ?: throw IllegalStateException(
                "DI is not initialized, you must call DI.initContainer(Context context) first."
            )
        }

        fun initContainer(context: Context): DependencyContainer = instance ?: synchronized(this) {
            instance ?: buildDI(context).also { instance = it }
        }

        private fun buildDI(context: Context): DependencyContainer = DependencyContainer(context)
    }
}