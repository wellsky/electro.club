package club.electro.ui.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import club.electro.dto.FeedPost
import club.electro.repository.FeedRepository
import club.electro.repository.FeedRepositoryInMemoryImpl
import kotlinx.coroutines.Dispatchers

class FeedViewModel : ViewModel() {
    private val repository: FeedRepository = FeedRepositoryInMemoryImpl()
    val data = repository.data.asLiveData(Dispatchers.Default)
}