package club.electro.ui.thread

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import club.electro.application.ElectroClubApp
import club.electro.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ThreadViewModel(application: Application, val threadId: Long) : AndroidViewModel(application) {

    private val repository: ThreadRepository = ThreadRepositoryServerImpl((application as ElectroClubApp).diContainer, threadId)

    val data = repository.data.asLiveData(Dispatchers.Default)
//
//    var threadId: Long = 0

//    init {
//        loadPosts()
//    }

    fun loadPosts() = viewModelScope.launch {
        try {

            //_dataState.value = FeedModelState(loading = true)

            repository.getThreadPosts()

            //_dataState.value = FeedModelState()
        } catch (e: Exception) {
            //_dataState.value = FeedModelState(error = true)
        }
    }
}