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
    ) {
        try {
            apiCall.invoke().body()?.let {
                onSuccess(it)
            }
            networkStatus.setStatus(NetworkStatus.Status.ONLINE)
        } catch (throwable: Throwable) {
            networkStatus.setStatus(NetworkStatus.Status.ERROR)
            onError.invoke()
        }
    }
}