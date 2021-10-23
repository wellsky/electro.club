package club.electro.ui.thread

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import club.electro.application.ElectroClubApp
import club.electro.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ThreadViewModel(application: Application, val threadType: Byte, val threadId: Long) : AndroidViewModel(application) {

    private val repository: ThreadRepository = ThreadRepositoryServerImpl((application as ElectroClubApp).diContainer, threadType, threadId)

    val data = repository.data.asLiveData(Dispatchers.Default)

    fun loadPosts() = viewModelScope.launch {
        try {
            repository.getThreadPosts()
        } catch (e: Exception) {
            //_dataState.value = FeedModelState(error = true)
        }
    }

    fun stop() {
        repository.stop()
    }
}