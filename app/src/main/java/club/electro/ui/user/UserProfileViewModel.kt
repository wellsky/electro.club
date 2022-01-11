package club.electro.ui.user

import androidx.lifecycle.*
import club.electro.dto.User
import club.electro.repository.UserRepository
import club.electro.repository.UserRepositoryServerImpl
import javax.inject.Inject

class UserProfileViewModel @Inject constructor(val userId: Long) : ViewModel() {
    private val repository: UserRepository = UserRepositoryServerImpl()
    val currentProfile: LiveData<User> = repository.getUserProfile(userId).asLiveData()
}