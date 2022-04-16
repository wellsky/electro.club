package club.electro.api

import club.electro.dto.*
import club.electro.utils.UrlDataResultDto
import club.electro.utils.UrlType
import club.electro.utils.urlTypeSerializer
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


/**
 * Кроме изменения основого адреса, надо добавить в манифест поддержку http, а также в токен-интерцепторе подстановку хоста
 * Отменить запросы к srv1 в SubscriptionsRepository и ThreadRepository
 */
const val BASE_SERVER_URL = "https://electro.club/api/v1/"
//const val BASE_SERVER_URL = "http://10.0.2.2:80/api/v1/"
const val UPDATES_SERVER_URL = "https://srv1.electro.club/api/"


fun okhttp(vararg interceptors: Interceptor): OkHttpClient = OkHttpClient.Builder()
    .apply {
        interceptors.forEach {
            this.addInterceptor(it)
        }
    }
    .build()

private val gson = GsonBuilder()
    .registerTypeAdapter(UrlType::class.java, urlTypeSerializer)
    .registerTypeAdapter(ThreadType::class.java, threadTypeSerializer)
    .create()

fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create(gson))
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
        @Field("group") group: Byte = 0,
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
    suspend fun setThreadVisit(
        @Field("method") method: String = "setThreadVisit",
        @Field("thread_type") threadType: Byte,
        @Field("thread_id") threadId: Long,
    ): Response<ApiResponse<Unit>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun getThreadPosts(
        @Field("method") method: String = "getPosts",
        @Field("thread_type") threadType: Byte,
        @Field("thread_id") threadId: Long,
        @Field("from") from: String? = null,
        @Field("included") included: Byte? = null,
        @Field("count") count: Int? = null,
        @Field("nearest") nearest: Int? = null,
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
    suspend fun getAreaStatus(
        @Field("method") method: String = "getAreaStatus",
        @Field("type") type: Byte,
        @Field("object_id") objectId: Long,
    ): Response<ApiResponse<ApiAreaStatus>>

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
    suspend fun setSocketStatus(
        @Field("method") method: String = "setSocketStatus",
        @Field("socket_id") socketId: Long,
        @Field("status") status: SocketStatus,
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
    suspend fun getPostAttachments(
        @Field("method") method: String = "getPostAttachments",
        @Field("thread_type") threadType: Byte,
        @Field("thread_id") threadId: Long,
        @Field("post_id") postId: Long?,
    ): Response<ApiResponse<List<PostAttachment>>>

    @Multipart
    @POST(BASE_SERVER_URL)
    suspend fun uploadPostAttachment(
        @Part("method") method: RequestBody = "uploadPostAttachment".toRequestBody(),
        @Part("thread_type") threadType: RequestBody,
        @Part("thread_id") threadId: RequestBody,
        @Part("post_id") postId: RequestBody,
        @Part("attachment_name") attachmentName: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<PostAttachment>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun removePostAttachment(
        @Field("method") method: String = "removePostAttachment",
        @Field("thread_type") threadType: Byte,
        @Field("thread_id") threadId: Long,
        @Field("attachment_id") attachmentId: Long?,
    ): Response<ApiResponse<Unit>>


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

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun getTransportList(
        @Field("method") method: String = "getTransportList",
        @Field("filter") filter: String,
    ): Response<ApiResponse<ApiTransportListData>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun getTransport(
        @Field("method") method: String = "getTransport",
        @Field("transport_id") transportId: Long,
    ): Response<ApiResponse<ApiTransportData>>

    @FormUrlEncoded
    @POST(BASE_SERVER_URL)
    suspend fun getChatWith(
        @Field("method") method: String = "getChatWith",
        @Field("user_id") userId: Long,
    ): Response<ApiResponse<ThreadLink>>
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
    val thread: PostsThread,
    @SerializedName("draft_text") val draftText: String?,
    @SerializedName("draft_attachments") val draftAttachments: List<PostAttachment>?
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

data class ApiTransportData (
    val transport: Transport,
    val discussions: List<Discussion>? = null
)

data class ApiTransportListData (
    val list: List<TransportPreview>
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
    val map_objects: List<MapMarker>
)

data class ApiAreaStatus (
    @SerializedName("modified_time") val lastUpdateTime: Long,
    @SerializedName("last_message_time") val lastMessageTime: Long,
    @SerializedName("messages_count") val messagesCount: Long,
)

data class ApiUserProfile (
    val user: User
)

data class ApiSavedPost (
    val message: Post
)

data class ApiSocketDetails (
    val socket: Socket
)