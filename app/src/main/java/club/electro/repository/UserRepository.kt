package club.electro.repository

import androidx.lifecycle.MutableLiveData
import club.electro.dto.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val currentProfile: Flow<User>

    suspend fun getLocalById(id: Long, onLoadedCallback:  (suspend () -> Unit)? = null): User?
    suspend fun getRemoteById(id: Long): User
    suspend fun setCurrentProfile(id: Long)
}