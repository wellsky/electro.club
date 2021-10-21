package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import club.electro.auth.AppAuth
import club.electro.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject


enum class LoginFormState {
    LOGGED, NOT_LOGGED, SUCCESS, ERROR
}

@HiltViewModel
@ExperimentalCoroutinesApi
class LoginViewModel @Inject constructor (
        private val repository: AccountRepository,
        private val appAuth: AppAuth
    ) : ViewModel() {
    private val _state = MutableLiveData<LoginFormState>()

    val state: LiveData<LoginFormState>
        get() = _state

    //val appAuth = AppAuth.getInstance()

    //private val repository: AccountRepository = AccountRepositoryServerImpl(application)

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