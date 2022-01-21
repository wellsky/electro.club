package club.electro.ui.map.socket

import androidx.lifecycle.*
import club.electro.dto.Socket
import club.electro.repository.MapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SocketViewModel @Inject constructor(
    val state : SavedStateHandle,
    val repository: MapRepository
) : ViewModel() {
    companion object {
        private val SOCKET_KEY = "socketId"
    }

    private val socketId: Long = state.get(SOCKET_KEY) ?: 0L

    val currentSocket: LiveData<Socket> = repository.getSocket(socketId).asLiveData()
}
