package club.electro.api

import club.electro.BuildConfig
import club.electro.di.DependencyContainer
import club.electro.dto.*
import club.electro.utils.UrlDataResult
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
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

// https://stackoverflow.com/questions/34791244/retrofit2-modifying-request-body-in-okhttp-interceptor
val addTokensInterceptor = Interceptor {
    val request = it.request()
    val body = request.body

    val diContainer = DependencyContainer.getInstance()

    val accessToken = diContainer.accessToken
    val userToken = if (diContainer.appAuth.myToken() != null) "&user_token=" + diContainer.appAuth.myToken() else ""

    val newRequest = request.newBuilder()
        .post(
            RequestBody.create(
                body?.contentType(),
                body.bodyToString() + "&access_token=" + accessToken + "&user_token=" + userToken
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


val okhttp = OkHttpClient.Builder()
        .addInterceptor(addTokensInterceptor)
        .addInterceptor(logging)
        .build()

val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_SERVER_URL)
    .client(okhttp)
    .build()

interface ApiService {
    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun getFeedPosts(@FieldMap params: HashMap<String?, String?>): Response<ApiResponse<ApiFeedPostsData>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun getUrlData(
        @Field("method") method: String = "getUrlData",
        @Field("url") url: String ,
    ): Response<ApiResponse<UrlDataResult>>

    //TODO перервести остальные запросы на такой формат вместо HashMap
    @FormUrlEncoded
    @POST(UPDATES_SERVER_URL)
    suspend fun getSubscriptions(
        @Field("method") method: String = "whatsUp",
        @Field("last_event_time") last_event_time: Long = 0
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

    // TODO изменить формат, чтобы метод API определялся внутри определения функции?
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

// TODO от API надо все получать не в CamelCase
// Еще возможно использовать аннотацию @SerializedName("nickname")
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


/**
Типы запросов:
https://stackoverflow.com/questions/47392832/retrofit2-use-body-vs-query

Версия с любыми параметрами:
@FormUrlEncoded
@POST(UPDATES_SERVER_URL)
suspend fun getSubscriptions(@FieldMap params: HashMap<String?, String?>): Response<ApiResponse<ApiSubscriptionsData>>

Как вызывать:
val params = HashMap<String?, String?>()
params["access_token"] = resources.getString(R.string.electro_club_access_token)
params["user_token"] = appAuth.myToken()
params["method"] = "whatsUp"
params["last_event_time"] = "0"
val response = apiService.getSubscriptions(params)
 */