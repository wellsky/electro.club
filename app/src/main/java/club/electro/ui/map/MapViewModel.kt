package club.electro.ui.map

import android.app.Application
import androidx.lifecycle.*
import club.electro.application.ElectroClubApp
import club.electro.repository.MapRepository
import club.electro.repository.MapRepositoryServerImpl
import club.electro.repository.ThreadRepository
import club.electro.repository.ThreadRepositoryServerImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MapRepository = MapRepositoryServerImpl((application as ElectroClubApp).diContainer)
    val data = repository.data.asLiveData(Dispatchers.Default)

    fun getAll() = viewModelScope.launch {
        repository.getAll()
    }
}