package club.electro.ui.user

import androidx.lifecycle.*
import club.electro.dto.User
import club.electro.repository.UserRepository
import javax.inject.Inject


class UserProfileViewModel @Inject constructor(
    val repository: UserRepository,
) : ViewModel() {
    val currentProfile: LiveData<User> = repository.getUserProfile().asLiveData()
}