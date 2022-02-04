package club.electro.ui.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import club.electro.repository.transport.TransportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransportListViewModel @Inject constructor (
    val repository: TransportRepository
): ViewModel() {
    val transportList = repository.list.asLiveData()

    init {
        viewModelScope.launch {
            repository.getPreviewList("")
        }
    }
}