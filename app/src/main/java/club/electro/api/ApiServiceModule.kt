package club.electro.api

import club.electro.BuildConfig
import club.electro.auth.AppAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Inject
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiServiceModule @Inject constructor () {
    companion object {
        @Provides
        @Singleton
        fun provideApi(retrofit: Retrofit): ApiService = retrofit.create()
    }

    @Provides
    @Singleton
    fun provideLogging(): Interceptor = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttp(
        //userRepository: UsersRepository,
        appAuth: AppAuth,
        logging: Interceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
//        .addInterceptor { chain ->
//            appAuth.authStateFlow.value.token?.let { token ->
//                val newRequest = chain.request().newBuilder()
//                    .addHeader("Authorization", token)
//                    .build()
//                return@addInterceptor chain.proceed(newRequest)
//            }
//            chain.proceed(chain.request())
//        }
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okhttp: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(okhttp)
        .build()
}