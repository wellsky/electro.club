package club.electro.ui.map.socket

import androidx.lifecycle.*
import club.electro.dto.Socket
import club.electro.dto.SocketStatus
import club.electro.repository.map.MapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocketViewModel @Inject constructor(
    val state : SavedStateHandle,
    val repository: MapRepository
) : ViewModel() {
    companion object {
        private const val SOCKET_KEY = "socketId"
    }

    private val socketId: Long = state.get(SOCKET_KEY) ?: 0L

    val currentSocket: LiveData<Socket?> = repository.observeSocket(socketId).asLiveData()

    init {
        viewModelScope.launch {
            repository.updateSocket(socketId)
        }
    }

    fun setSocketStatus(socketId: Long, status: SocketStatus) {
        viewModelScope.launch {
            repository.setSocketStatus(socketId, status)
        }
    }
}
