package club.electro.ui.user

import androidx.lifecycle.*
import club.electro.auth.AppAuth
import club.electro.dto.ThreadLink
import club.electro.dto.User
import club.electro.repository.thread.ThreadRepository
import club.electro.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    state : SavedStateHandle,
    val userRepository: UserRepository,
    val appAuth: AppAuth
) : ViewModel() {

    companion object {
        private const val USER_KEY = "userId"
    }

    val userId: Long = state.get(USER_KEY) ?: 0L

    val currentProfile: LiveData<User> = userRepository.getUserProfile(userId).asLiveData()

    fun getChatWith(userId: Long): LiveData<ThreadLink?> = flow {
            emit(userRepository.getChatWith(userId))
    }.asLiveData()
}