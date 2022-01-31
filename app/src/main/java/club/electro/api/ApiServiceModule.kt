package club.electro.api

import android.content.Context
import club.electro.auth.AppAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import javax.inject.Inject
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ApiServiceModule{
    @Provides
    @Singleton
    fun provideApiService(
        tokenInterceptor: Interceptor
    ): ApiService {
        return retrofit(okhttp(
                loggingInterceptor(),
                tokenInterceptor
        ))
        .create(ApiService::class.java)
    }
}