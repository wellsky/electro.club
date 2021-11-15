package club.electro.ui.user

import android.app.Application
import androidx.lifecycle.*
import club.electro.application.ElectroClubApp
import club.electro.dto.EmptyUserProfile
import club.electro.dto.PostsThread
import club.electro.dto.User
import club.electro.repository.ThreadRepository
import club.electro.repository.ThreadRepositoryServerImpl
import club.electro.repository.UserRepository
import club.electro.repository.UserRepositoryServerImpl
import kotlinx.coroutines.launch

class ThreadInfoViewModel(application: Application, threadType: Byte, threadId: Long) : AndroidViewModel(application) {
    private val repository: ThreadRepository = ThreadRepositoryServerImpl((application as ElectroClubApp).diContainer, threadType, threadId)

    var thread: LiveData<PostsThread> = repository.thread.asLiveData()
    val lastUpdateTime = repository.lastUpdateTime

    fun getThread() {
        viewModelScope.launch {
            try {
                repository.getThread()
            } catch (e: Exception) {

            }
        }
    }

    fun stopCheckUpdates() {
        repository.stopCheckUpdates()
    }
}