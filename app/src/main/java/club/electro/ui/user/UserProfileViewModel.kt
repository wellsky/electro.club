package club.electro.ui.user

import android.app.Application
import androidx.lifecycle.*
import club.electro.application.ElectroClubApp
import club.electro.dto.User
import club.electro.repository.UserRepository
import club.electro.repository.UserRepositoryServerImpl
import kotlinx.coroutines.launch

class UserProfileViewModel(application: Application, userId: Long) : AndroidViewModel(application) {
    private val repository: UserRepository = UserRepositoryServerImpl((application as ElectroClubApp).diContainer)
    var currentProfile: LiveData<User> = repository.getUserProfile(userId).asLiveData()
}