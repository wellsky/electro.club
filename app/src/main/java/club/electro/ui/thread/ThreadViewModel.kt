package club.electro.ui.thread

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import club.electro.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
//    object : ViewModelProvider.Factory {
//        override fun <T : ViewModel> create(aClass: Class<T>):T = f() as T
//    }
//
//inline fun <reified T : ViewModel> Fragment.viewModelsFactory(crossinline viewModelInitialization: () -> T): Lazy<T> {
//    return viewModels {
//        object : ViewModelProvider.Factory {
//            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//                return viewModelInitialization.invoke() as T
//            }
//        }
//    }
//}

class ThreadViewModel(application: Application, val threadId: Long) : AndroidViewModel(application) {

    private val repository: ThreadRepository = ThreadRepositoryServerImpl(application, threadId)

    val data = repository.data.asLiveData(Dispatchers.Default)
//
//    var threadId: Long = 0

//    init {
//        loadPosts()
//    }

    fun loadPosts() = viewModelScope.launch {
        try {
            println("load posts for: " + threadId)
            //_dataState.value = FeedModelState(loading = true)

            repository.getThreadPosts()

            //_dataState.value = FeedModelState()
        } catch (e: Exception) {
            //_dataState.value = FeedModelState(error = true)
        }
    }
}