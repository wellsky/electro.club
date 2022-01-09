package club.electro.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import club.electro.di.DependencyContainer
import club.electro.repository.FeedRepository
import club.electro.repository.FeedRepositoryServerImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {
    private val repository: FeedRepository = FeedRepositoryServerImpl()
    val data = repository.data.asLiveData(Dispatchers.Default)

    fun getFeedPosts() = viewModelScope.launch {
        repository.getFeedPosts()
    }
}