package club.electro.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import club.electro.dto.FeedPost
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    val data: Flow<List<FeedPost>>
    fun getAll()
}