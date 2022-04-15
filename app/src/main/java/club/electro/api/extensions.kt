package club.electro.api

import club.electro.error.ApiError
import club.electro.model.NetworkStatus
import retrofit2.Response


fun <T> Response<T>.checkIfOk(networkStatus: NetworkStatus): T? {
    if (!this.isSuccessful) {
        //throw ApiError(this.code(), this.message())
        networkStatus.setStatus(NetworkStatus.Status.ERROR)
    }

    return if (this.body() == null) {
        networkStatus.setStatus(NetworkStatus.Status.ERROR)
        null
    } else {
        networkStatus.setStatus(NetworkStatus.Status.ONLINE)
        this.body()
    }
}