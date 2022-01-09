package club.electro.application

import android.app.Application
import android.content.Context
import club.electro.auth.AppAuth
import club.electro.di.DependencyContainer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class ElectroClubApp: Application() {
    lateinit var diContainer: DependencyContainer

    override fun onCreate() {
        super.onCreate()
        diContainer = DependencyContainer.initContainer()

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
    }
}