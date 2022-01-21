package club.electro.api

import club.electro.dto.*
import club.electro.utils.UrlDataResult
import club.electro.utils.UrlDataResultDto
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

const val BASE_SERVER_URL = "https://electro.club/api/v1/"
const val UPDATES_SERVER_URL = "https://srv1.electro.club/api/"


fun okhttp(vararg interceptors: Interceptor): OkHttpClient = OkHttpClient.Builder()
    .apply {
        interceptors.forEach {
            this.addInterceptor(it)
        }
    }
    .build()

fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_SERVER_URL)
    .client(client)
    .build()


interface ApiService {
    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun getFeedPosts(@Field("method") method: String = "getFeedPosts"): Response<ApiResponse<ApiFeedPostsData>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun getUrlData(
        @Field("method") method: String = "getUrlData",
        @Field("url") url: String,
    ): Response<ApiResponse<UrlDataResultDto>>

    @FormUrlEncoded
    @POST(UPDATES_SERVER_URL)
    suspend fun getSubscriptions(
        @Field("method") method: String = "whatsUp",
        @Field("lastEventTime") lastEventTime: Long = 0
    ): Response<ApiResponse<ApiSubscriptionsData>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun getThread(
        @Field("method") method: String = "getThread",
        @Field("thread_type") threadType: Byte,
        @Field("thread_id") threadId: Long,
    ): Response<ApiResponse<ApiThreadData>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun getThreadPosts(
        @Field("method") method: String = "getPosts",
        @Field("thread_type") threadType: Byte,
        @Field("thread_id") threadId: Long,
        @Field("from") from: String? = null,
        @Field("included") included: Byte? = null,
        @Field("count") count: Int? = null,
    ): Response<ApiResponse<ApiPostsData>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun signIn(
        @Field("method") method: String = "login",
        @Field("email") email: String,
        @Field("password") password: String,
    ): Response<ApiResponse<ApiAccountData>>

    @FormUrlEncoded
    @POST(UPDATES_SERVER_URL)
    suspend fun getAreaModifiedTime(
        @Field("method") method: String = "getAreaModifiedTime",
        @Field("type") type: Byte,
        @Field("object_id") objectId: Long,
    ): Response<ApiResponse<ApiAreaLastUpdateTime>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun getMapObjects(
        @Field("method") method: String = "getMapObjects",
        @Field("types") types: String,
    ): Response<ApiResponse<ApiMapObjects>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun getSocketDetails(
        @Field("method") method: String = "getSocketDetails",
        @Field("socket_id") socketId: Long,
    ): Response<ApiResponse<ApiSocketDetails>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun savePost(
        @Field("method") method: String = "savePost",
        @Field("thread_type") threadType: Byte,
        @Field("thread_id") threadId: Long,
        @Field("post_id") postId: Long?,
        @Field("post_content") postContent: String,
        @Field("answer_to") answerTo: Long?,
    ): Response<ApiResponse<ApiSavedPost>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun removePost(
        @Field("method") method: String = "removePost",
        @Field("thread_type") threadType: Byte,
        @Field("thread_id") threadId: Long,
        @Field("post_id") postId: Long?,
    ): Response<ApiResponse<Unit>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun changeSubscription(
        @Field("method") method: String = "changeSubscription",
        @Field("thread_type") threadType: Byte,
        @Field("thread_id") threadId: Long,
        @Field("status") status: Byte,
    ): Response<ApiResponse<ApiThreadData>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun setPushToken(
        @FieldMap params: HashMap<String?, String?>
    ): Response<Unit>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun getUserProfile(
        @Field("method") method: String = "getUserProfile",
        @Field("user_id") userId: Long,
    ): Response<ApiResponse<ApiUserProfile>>

}

data class ApiResponse<D> (
    val status: String,
    val data: D
)

data class ApiSubscriptionsData (
    val method: String,
    val lastEventTime: Long,
    val items: List<SubscriptionArea> = emptyList()
)

data class ApiThreadData (
    val thread: PostsThread
)

data class ApiPostsData (
    val messages: List<Post>
)

data class ApiFeedPostsData (
    val messages: List<FeedPost>
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
    val transport_name: String?,
    val transport_image: String?
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

data class ApiSavedPost (
    val message: Post
)

data class ApiSocketDetails (
    val socket: Socket
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