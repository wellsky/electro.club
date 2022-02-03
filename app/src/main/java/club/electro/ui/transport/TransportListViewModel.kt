package club.electro.ui.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import club.electro.repository.transport.TransportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TransportListViewModel @Inject constructor (
    val repository: TransportRepository
): ViewModel() {
    val transportList = repository.getTransportPreview("").asLiveData()
}