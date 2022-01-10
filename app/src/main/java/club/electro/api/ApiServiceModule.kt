package club.electro.api

import android.content.Context
import club.electro.auth.AppAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ApiServiceModule{
    @ApplicationContext
    lateinit var context: Context
    @Inject
    lateinit var appAuth : AppAuth

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return retrofit(okhttp(loggingInterceptor(), addTokensInterceptor(context, appAuth)))
            .create(ApiService::class.java)
    }
}