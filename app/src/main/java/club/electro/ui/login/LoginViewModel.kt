package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import club.electro.application.ElectroClubApp
import club.electro.auth.AppAuth
import club.electro.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LoginFormState {
    LOGGED, NOT_LOGGED, SUCCESS, ERROR
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    val appAuth: AppAuth,
    val repository: AccountRepository,
) : ViewModel() {
    private val _state = MutableLiveData<LoginFormState>()

    val state: LiveData<LoginFormState>
        get() = _state

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