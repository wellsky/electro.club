package club.electro.api

import club.electro.BuildConfig
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
fun addTokensInterceptor() = Interceptor {
    val request = it.request()
    val body = request.body

    val diContainer = DependencyContainer.getInstance()

    val accessTokenString = "&access_token=" + diContainer.accessToken
    val userTokenString = if (diContainer.appAuth.myToken() != null) "&user_token=" + diContainer.appAuth.myToken() else ""

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
