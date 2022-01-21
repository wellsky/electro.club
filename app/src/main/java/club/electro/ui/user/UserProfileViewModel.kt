package club.electro.ui.user

import androidx.lifecycle.*
import club.electro.dto.User
import club.electro.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    state : SavedStateHandle,
    val repository: UserRepository,
) : ViewModel() {

    companion object {
        private val USER_KEY = "userId"
    }

    val userId: Long = state.get(USER_KEY) ?: 0L
    val currentProfile: LiveData<User> = repository.getUserProfile(userId).asLiveData()
}