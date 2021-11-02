package club.electro.ui.user

import android.app.Application
import androidx.lifecycle.*
import club.electro.application.ElectroClubApp
import club.electro.dto.EmptyUserProfile
import club.electro.dto.User
import club.electro.repository.UserRepository
import club.electro.repository.UserRepositoryServerImpl
import kotlinx.coroutines.launch

class UserProfileViewModel(application: Application, userId: Long) : AndroidViewModel(application) {
    private val repository: UserRepository = UserRepositoryServerImpl((application as ElectroClubApp).diContainer, userId)

    var currentProfile: LiveData<User> = repository.currentProfile.asLiveData()

    fun getUserProfile(id: Long) {
        viewModelScope.launch {
            try {
                repository.getUserProfile(id)
            } catch (e: Exception) {

            }
        }
    }
}