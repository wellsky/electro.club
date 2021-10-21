package club.electro.ui.subscriptions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import club.electro.repository.SubscriptionsRepository
import club.electro.repository.SubscriptionsRepositoryServerImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class SubscriptionsViewModel @Inject constructor(
        private val repository: SubscriptionsRepository
    ) : ViewModel() {
    //private val repository: SubscriptionsRepository = SubscriptionsRepositoryServerImpl(application)
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