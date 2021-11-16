package club.electro.repository

import androidx.lifecycle.MutableLiveData
import club.electro.dto.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val currentProfile: Flow<User>

    suspend fun getLocalById(id: Long, callback: () -> Unit): User?

    suspend fun loadUser(id: Long): User

    suspend fun setCurrentProfile(id: Long)
}