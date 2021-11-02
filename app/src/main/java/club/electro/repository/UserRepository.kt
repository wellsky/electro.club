package club.electro.repository

import androidx.lifecycle.MutableLiveData
import club.electro.dto.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val currentProfile: Flow<User>
    suspend fun getUserProfile(id: Long)
}