package club.electro.api

import android.content.Context
import club.electro.BuildConfig
import club.electro.R
import club.electro.auth.AppAuth
import club.electro.di.DependencyContainer
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor

fun loggingInterceptor() = HttpLoggingInterceptor()
    .apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }


// https://stackoverflow.com/questions/34791244/retrofit2-modifying-request-body-in-okhttp-interceptor
fun addTokensInterceptor(context: Context, appAuth: AppAuth) = Interceptor {
    val request = it.request()
    val body = request.body

    val accessToken = context.resources.getString(R.string.electro_club_access_token)

    val accessTokenString = "&access_token=" + accessToken
    val userTokenString = if (appAuth.myToken() != null) "&user_token=" + appAuth.myToken() else ""

    val newRequest = request.newBuilder()
        .post(
            RequestBody.create(
                body?.contentType(),
                body.bodyToString() + accessTokenString + userTokenString
            )
        )
        .build()
    it.proceed(newRequest)
}

fun RequestBody?.bodyToString(): String {
    if (this == null) return ""
    val buffer = okio.Buffer()
    writeTo(buffer)
    return buffer.readUtf8()
}
