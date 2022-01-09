package club.electro.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkStatus @Inject constructor() {
    enum class Status {
        ONLINE, OFFLINE, ERROR
    }

    companion object {
        @Volatile
        private var instance: NetworkStatus? = null

        fun getInstance(): NetworkStatus {
            return instance ?: synchronized(this) {
                instance ?: NetworkStatus().also { instance = it }
            }
        }
    }

    val _status = MutableStateFlow(Status.ONLINE)
    val status: StateFlow<Status>
        get() = _status

    fun setStatus(status: Status) {
        if (status != _status.value) {
            _status.value = status
        }
    }
}