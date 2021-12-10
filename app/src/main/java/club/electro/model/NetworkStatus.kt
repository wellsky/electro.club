package club.electro.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NetworkStatus {
    companion object {
        val STATUS_ONLINE: Byte = 0
        val STATUS_OFFLINE: Byte  = 1
        val STATUS_ERROR: Byte  = 2

        @Volatile
        private var instance: NetworkStatus? = null

        fun getInstance(): NetworkStatus {
            return NetworkStatus.instance ?: synchronized(this) {
                NetworkStatus.instance ?: NetworkStatus().also { NetworkStatus.instance = it }
            }
        }
    }

    val _status = MutableLiveData(STATUS_ONLINE)
    val status: LiveData<Byte>
        get() = _status

    fun setStatus(status: Byte) {
        if (status != _status.value) {
            _status.postValue(status)
        }
    }
}