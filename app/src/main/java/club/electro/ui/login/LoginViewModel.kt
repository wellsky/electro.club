package ru.netology.nmedia.viewmodel

import AccountRepositoryServerImpl
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import club.electro.application.ElectroClubApp
import club.electro.auth.AppAuth
import club.electro.repository.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


enum class LoginFormState {
    LOGGED, NOT_LOGGED, SUCCESS, ERROR
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableLiveData<LoginFormState>()

    val state: LiveData<LoginFormState>
        get() = _state

    //val appAuth = application.diContainer //AppAuth.getInstance()
    val appAuth = (application as ElectroClubApp).diContainer.appAuth


    private val repository: AccountRepository = AccountRepositoryServerImpl((application as ElectroClubApp).diContainer)

    init {
        if (appAuth.myId() == 0L) {
            _state.value = LoginFormState.NOT_LOGGED
        } else {
            _state.value = LoginFormState.LOGGED
        }
    }

    fun signIn(login: String, password: String) = viewModelScope.launch {
        try {
            repository.signIn(login, password)
            _state.value = LoginFormState.SUCCESS
        } catch (e: Exception) {
            _state.value = LoginFormState.ERROR
        }
    }

    fun signOut() = viewModelScope.launch {
        appAuth.removeAuth()
        repository.signOut()
        _state.value = LoginFormState.NOT_LOGGED
    }
}