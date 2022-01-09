package club.electro.api

import club.electro.auth.AppAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ApiServiceModule {
    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return retrofit(okhttp(loggingInterceptor(), addTokensInterceptor()))
            .create(ApiService::class.java)
    }
}