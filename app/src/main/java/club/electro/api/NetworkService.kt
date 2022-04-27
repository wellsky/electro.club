package club.electro.api

import club.electro.model.NetworkStatus
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkService @Inject constructor(
    val networkStatus: NetworkStatus,
) {
    suspend fun <T> safeApiCall(
            apiCall: suspend () -> Response<T>,
            onSuccess: suspend(body: T) -> Unit = {},
            onError: suspend() -> Unit = {},
    ): T? {
        return try {
            apiCall.invoke().body()?.let {
                networkStatus.setStatus(NetworkStatus.Status.ONLINE)
                onSuccess(it)
                it
            }
        } catch (throwable: Throwable) {
            networkStatus.setStatus(NetworkStatus.Status.ERROR)
            onError.invoke()
            null
        }
    }
}