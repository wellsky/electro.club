package club.electro.repository.user

import androidx.lifecycle.MutableLiveData
import club.electro.dto.ThreadLink
import club.electro.dto.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getLocalById(id: Long, onLoadedCallback:  (suspend () -> Unit)? = null): User?
    suspend fun getRemoteById(id: Long): User?
    fun getUserProfile(id: Long): Flow<User>

    suspend fun getChatWith(userId: Long): ThreadLink?
}