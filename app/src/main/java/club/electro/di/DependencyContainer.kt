package club.electro.di

import android.content.Context
import androidx.room.Room
import club.electro.api.Api
import club.electro.api.ApiService
import club.electro.auth.AppAuth
import club.electro.db.AppDb
import club.electro.repository.PostRepository
import club.electro.repository.PostRepositoryServerImpl

class DependencyContainer private constructor(val context: Context) {
    val appDb = AppDb.getInstance(context = context)
    val apiService: ApiService = Api.service
    val appAuth: AppAuth = AppAuth.initApp(context, this)
    val postRepository: PostRepository = PostRepositoryServerImpl(this)

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