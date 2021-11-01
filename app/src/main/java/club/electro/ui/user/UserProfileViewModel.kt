package club.electro.ui.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import club.electro.application.ElectroClubApp
import club.electro.dto.EmptyUserProfile
import club.electro.dto.UserProfile
import club.electro.repository.UserRepository
import club.electro.repository.UserRepositoryServerImpl
import kotlinx.coroutines.launch

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository = UserRepositoryServerImpl((application as ElectroClubApp).diContainer)

    var currentProfile: MutableLiveData<UserProfile> = MutableLiveData(EmptyUserProfile())

    init {
        getUserProfile(1)
    }

    fun getUserProfile(id: Long) {
        viewModelScope.launch {
            try {
                currentProfile.value = repository.getUserProfile(id)
            } catch (e: Exception) {

            }
        }
    }
}