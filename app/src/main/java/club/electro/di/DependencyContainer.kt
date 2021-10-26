package club.electro.di

import android.content.Context
import club.electro.api.Api
import club.electro.api.ApiService
import club.electro.auth.AppAuth
import club.electro.db.AppDb

class DependencyContainer(val context: Context) {
    val appDb = AppDb.getInstance(context = context)
    val apiService: ApiService = Api.service
    val appAuth: AppAuth = AppAuth.initApp(context, this)
}