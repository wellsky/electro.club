package club.electro.api

import club.electro.dto.Post
import club.electro.dto.SubscriptionArea
import retrofit2.Response
import retrofit2.http.*

const val BASE_URL = "https://srv1.electro.club"

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

//object Api {
//    val service: ApiService by lazy {
//        retrofit.create(ApiService::class.java)
//    }
//}

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