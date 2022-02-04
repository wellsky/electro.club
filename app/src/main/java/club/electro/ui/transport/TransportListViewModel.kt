package club.electro.ui.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import club.electro.repository.transport.TransportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransportListViewModel @Inject constructor (
    val repository: TransportRepository
): ViewModel() {
    companion object {
        val SEARCH_DELAY = 1500L
    }

    val transportList = repository.list.asLiveData()
    private var searchQueue: Job = viewModelScope.launch {};

    init {
        viewModelScope.launch {
            repository.getPreviewList("")
        }
    }

    fun queueNewSearch(filter: String) {
        searchQueue.cancel()
        searchQueue = viewModelScope.launch {
            delay(SEARCH_DELAY)
            repository.getPreviewList(filter)
            repository.setPreviewListFilter(filter)
        }
    }
}