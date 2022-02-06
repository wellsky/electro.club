package club.electro.ui.transport

import androidx.lifecycle.*
import club.electro.dto.Transport
import club.electro.repository.transport.TransportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TransportViewModel @Inject constructor(
    state : SavedStateHandle,
    val repository: TransportRepository,
) : ViewModel() {

    companion object {
        private val TRANSPORT_KEY = "transportId"
    }

    val transportId: Long = state.get(TRANSPORT_KEY) ?: 0L
    val currentTransport: LiveData<Transport> = repository.getTransportById(transportId).asLiveData()
}