package club.electro.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import club.electro.repository.FeedRepository
import club.electro.repository.FeedRepositoryServerImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: FeedRepository
)
: ViewModel() {
    val data = repository.data.asLiveData(Dispatchers.Default)

    fun getFeedPosts() = viewModelScope.launch {
        repository.getFeedPosts()
    }
}