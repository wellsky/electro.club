package club.electro.di

import android.content.Context
import androidx.work.WorkManager
import club.electro.R
import club.electro.auth.AppAuth
import club.electro.db.AppDb
import club.electro.model.NetworkStatus
import club.electro.repository.PostRepository
import club.electro.repository.PostRepositoryServerImpl
import club.electro.repository.UserRepository
import club.electro.repository.UserRepositoryServerImpl
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DependencyContainer @Inject constructor() {
    @ApplicationContext lateinit var context: Context
    @Inject lateinit var appAuth: AppAuth

    val resources = context.resources
    val accessToken = resources.getString(R.string.electro_club_access_token)

    //val appDb = AppDb.getInstance(context = context)

    //lateinit var apiService: ApiService = Api.service


    //val postDao = appDb.postDao()

    // val networkStatus = NetworkStatus.getInstance()

    //val postRepository: PostRepository = PostRepositoryServerImpl(this)
    //val userRepository: UserRepository = UserRepositoryServerImpl(this)

    //val workManager = WorkManager.getInstance(context)



//    init {
//        // TODO криво реализованная взаимная инъекция?
//        postRepository.setupWorkManager(workManager)
//    }

    companion object {
        @Volatile
        private var instance: DependencyContainer? = null

        fun getInstance(): DependencyContainer = synchronized(this) {
            instance ?: throw IllegalStateException(
                "DI is not initialized, you must call DI.initContainer(Context context) first."
            )
        }

        fun initContainer(): DependencyContainer = instance ?: synchronized(this) {
            instance ?: buildDI().also { instance = it }
        }

        private fun buildDI(): DependencyContainer = DependencyContainer()
    }
}