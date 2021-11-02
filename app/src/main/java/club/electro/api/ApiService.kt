package club.electro.api

import club.electro.BuildConfig
import club.electro.dao.AreaDao
import club.electro.dto.*
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

const val BASE_SERVER_URL = "https://electro.club/api/v1/"
const val UPDATES_SERVER_URL = "https://srv1.electro.club/api/"

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
    .baseUrl(BASE_SERVER_URL)
    .client(okhttp)
    .build()

interface ApiService {
    @FormUrlEncoded
    @POST(UPDATES_SERVER_URL)
    suspend fun getSubscriptions(@FieldMap params: HashMap<String?, String?>): Response<ApiResponse<ApiSubscriptionsData>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun getThreadPosts(@FieldMap params: HashMap<String?, String?>): Response<ApiResponse<ApiPostsData>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun signIn(@FieldMap params: HashMap<String?, String?>): Response<ApiResponse<ApiAccountData>>

    @FormUrlEncoded
    @POST(UPDATES_SERVER_URL)
    suspend fun getAreaModifiedTime(@FieldMap params: HashMap<String?, String?>): Response<ApiResponse<ApiAreaLastUpdateTime>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun getMapObjects(@FieldMap params: HashMap<String?, String?>): Response<ApiResponse<ApiMapObjects>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun savePost(@FieldMap params: HashMap<String?, String?>): Response<ApiResponse<ApiMapObjects>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun setPushToken(@FieldMap params: HashMap<String?, String?>): Response<Unit>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun getUserProfile(@FieldMap params: HashMap<String?, String?>): Response<ApiResponse<ApiUserProfile>>

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

data class ApiMapObjects(
    val mapObjects: List<MapMarker>
)

data class ApiAreaLastUpdateTime (
    val time: Long,
)

data class ApiUserProfile (
    val user: ApiUserProfileData
)

data class ApiUserProfileData (
    val user_id: Long,
    val nickname: String,
    val thumbnail: String,
    val account_created: Long,
    val last_visit: Long,
    val messages: Int,
    val rating: Int,
    val primaryTransport: UserPrimaryTransport?,
    val myChat: ThreadLink?
) {
    fun toDto() = User (
        id = user_id,
        name = nickname,
        avatar = thumbnail,
        messages = messages,
        rating = rating,
        primaryTransport = primaryTransport,
        myChat = myChat
    )
}