package club.electro.ui.transport

import androidx.lifecycle.*
import club.electro.dto.Discussion
import club.electro.dto.Transport
import club.electro.repository.transport.TransportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
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

    fun transportDiscussions(transportId: Long): LiveData<List<Discussion>>
        = repository.getDiscussionsByTransportId(transportId).flowOn(Dispatchers.Default).asLiveData()

}