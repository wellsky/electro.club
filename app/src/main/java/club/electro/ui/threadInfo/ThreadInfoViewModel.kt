package club.electro.ui.user

import androidx.lifecycle.*
import club.electro.dto.PostsThread
import club.electro.repository.ThreadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThreadInfoViewModel @Inject constructor(
    val repository: ThreadRepository
) : ViewModel() {
    var thread: LiveData<PostsThread> = repository.thread.asLiveData()
    val lastUpdateTime = repository.lastUpdateTime

    fun getThread() = viewModelScope.launch {
        repository.getThread()
    }

    fun startCheckUpdates() {
        repository.startCheckUpdates()
    }

    fun stopCheckUpdates() {
        repository.stopCheckUpdates()
    }
}