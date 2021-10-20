package club.electro.api

import club.electro.BuildConfig
import club.electro.dao.AreaDao
import club.electro.dto.Post
import club.electro.dto.SubscriptionArea
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

const val BASE_URL = "https://srv1.electro.club"

val logging = HttpLoggingInterceptor().apply {
    if (BuildConfig.DEBUG) {
        level = HttpLoggingInterceptor.Level.BODY
    }
}

val okhttp = OkHttpClient.Builder()
    .addInterceptor(logging)
//    .addInterceptor { chain ->
//        AppAuth.getInstance().authStateFlow.value.token?.let { token ->
//            val newRequest = chain.request().newBuilder()
//                .addHeader("Authorization", token)
//                .build()
//            return@addInterceptor chain.proceed(newRequest)
//        }
//        chain.proceed(chain.request())
//    }
    .build()

val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(okhttp)
    .build()

interface ApiService {
    @FormUrlEncoded
    @POST("/api")
    suspend fun getSubscriptions(@FieldMap params: HashMap<String?, String?>): Response<ApiResponse<ApiSubscriptionsData>>

    @FormUrlEncoded
    @POST("https://electro.club/api/v1")
    suspend fun getThreadPosts(@FieldMap params: HashMap<String?, String?>): Response<ApiResponse<ApiPostsData>>

    @FormUrlEncoded
    @POST("https://electro.club/api/v1")
    suspend fun signIn(@FieldMap params: HashMap<String?, String?>): Response<ApiResponse<ApiAccountData>>

}

object Api {
    val service: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}

data class ApiResponse<D> (
    val status: String,
    val data: D
)

data class ApiSubscriptionsData (
    val method: String,
    val lastEventTime: String,
    val items: List<SubscriptionArea>
)

data class ApiPostsData (
    val messages: List<Post>
)

data class ApiAccountData (
    val user: ApiAccountUserData
)

data class ApiAccountUserData (
    val user_token: String,
    val email: String,
    val user_id: Long,
    val nickname: String,
    val thumbnail: String,
    val account_created: Long,
    val last_visit: Long,
)