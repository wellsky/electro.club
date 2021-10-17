package club.electro.ui.subscriptions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import club.electro.repository.SubscriptionsRepository
import club.electro.repository.SubscriptionsRepositoryInMemoryImpl
import club.electro.repository.SubscriptionsRepositoryServerImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb

class SubscriptionsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SubscriptionsRepository = SubscriptionsRepositoryServerImpl(application)
    val data = repository.data.asLiveData(Dispatchers.Default)

    init {
        loadPosts()
    }


    fun loadPosts() = viewModelScope.launch {
        try {
            //_dataState.value = FeedModelState(loading = true)
            repository.getAll()
            //_dataState.value = FeedModelState()
        } catch (e: Exception) {
            //_dataState.value = FeedModelState(error = true)
        }
    }
}