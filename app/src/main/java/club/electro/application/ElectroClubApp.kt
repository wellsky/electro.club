package club.electro.application

import android.app.Application
import android.content.Context
import club.electro.auth.AppAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ElectroClubApp: Application() {
    private val appScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        setupAuth()

//        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
//        println("Shared prefs auth:")
//        println(prefs.getLong("id", 0))
//        println(prefs.getString("token", null))
//        println(prefs.getString("name", null))
//        println(prefs.getString("avatar", null))
    }

    private fun setupAuth() {
        appScope.launch {
            AppAuth.initApp(this@ElectroClubApp)
        }
    }
}
