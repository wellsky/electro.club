package club.electro.ui.user

import android.app.Application
import androidx.lifecycle.*
import club.electro.application.ElectroClubApp
import club.electro.dto.PostsThread
import club.electro.repository.ThreadRepository
import club.electro.repository.ThreadRepositoryServerImpl
import kotlinx.coroutines.launch

class ThreadInfoViewModel(application: Application, threadType: Byte, threadId: Long) : AndroidViewModel(application) {
    private val repository: ThreadRepository = ThreadRepositoryServerImpl((application as ElectroClubApp).diContainer, threadType, threadId)

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