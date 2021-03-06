package club.electro.api

import android.content.Context
import club.electro.BuildConfig
import club.electro.R
import club.electro.auth.AppAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

fun loggingInterceptor() = HttpLoggingInterceptor()
    .apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }


abstract class TokenInterceptor: Interceptor

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {
    @Singleton
    @Provides
    fun provideTokensInterceptor(
        @ApplicationContext context: Context,
        appAuth: AppAuth
    ) = Interceptor {
            // https://stackoverflow.com/questions/34791244/retrofit2-modifying-request-body-in-okhttp-interceptor
            val request = it.request()

            val accessToken =
                context.resources.getString(R.string.electro_club_access_token)

            val requestBuilder = request.newBuilder()
//                .header("Host", "electro.local")
                .header("ACCESS-TOKEN", accessToken)


            appAuth.myToken()?.let {
                requestBuilder.header("USER-TOKEN", it)
            }
            val newRequest = requestBuilder.build()
            it.proceed(newRequest)
        }
}

fun RequestBody?.bodyToString(): String {
    if (this == null) return ""
    val buffer = okio.Buffer()
    writeTo(buffer)
    return buffer.readUtf8()
}
