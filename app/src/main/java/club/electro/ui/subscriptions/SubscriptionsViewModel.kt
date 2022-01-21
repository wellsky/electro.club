package club.electro.ui.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import club.electro.auth.AppAuth
import club.electro.repository.SubscriptionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    val repository: SubscriptionsRepository,
    val appAuth: AppAuth
) : ViewModel() {
    val data = repository.data.asLiveData(Dispatchers.Default)

    init {
        loadSubscriptions()
    }

    fun loadSubscriptions() = viewModelScope.launch {
        repository.getAll()
    }

    fun startCheckUpdates() {
        repository.startCheckUpdates()
    }

    fun stopCheckUpdates() {
        repository.stopCheckUpdates()
    }
}