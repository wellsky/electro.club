package club.electro.ui.thread

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import club.electro.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class ThreadViewModel @Inject constructor (
    private val threadId: Long,
    private val repository: ThreadRepository
    ): ViewModel() {

    //private val repository: ThreadRepository = ThreadRepositoryServerImpl(threadId)

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