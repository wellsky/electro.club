package club.electro.application

import android.app.Application
import android.content.Context
import club.electro.auth.AppAuth
import club.electro.di.DependencyContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ElectroClubApp: Application() {
    lateinit var diContainer: DependencyContainer

    override fun onCreate() {
        super.onCreate()
        diContainer = DependencyContainer.initContainer(this)

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
    }
}