package club.electro.ui.map.socket

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import club.electro.application.ElectroClubApp
import club.electro.dto.Socket
import club.electro.repository.MapRepository
import club.electro.repository.MapRepositoryServerImpl

class SocketViewModel(application: Application, socketId: Long) : AndroidViewModel(application) {
    private val repository: MapRepository = MapRepositoryServerImpl((application as ElectroClubApp).diContainer)
    val currentSocket: LiveData<Socket> = repository.getSocket(socketId).asLiveData()
}
