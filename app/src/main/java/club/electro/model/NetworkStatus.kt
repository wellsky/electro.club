package club.electro.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class NetworkStatus {
    enum class Status {
        ONLINE, OFFLINE, ERROR
    }

    companion object {
        @Volatile
        private var instance: NetworkStatus? = null

        fun getInstance(): NetworkStatus {
            return NetworkStatus.instance ?: synchronized(this) {
                NetworkStatus.instance ?: NetworkStatus().also { NetworkStatus.instance = it }
            }
        }
    }

    val _status = MutableLiveData(Status.ONLINE)
    val status: LiveData<Status>
        get() = _status

    fun setStatus(status: Status) {
        if (status != _status.value) {
            _status.postValue(status)
        }
    }
}