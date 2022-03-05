package club.electro.ui.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import club.electro.auth.AppAuth
import club.electro.repository.subscriptions.SubscriptionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    val repository: SubscriptionsRepository,
    val appAuth: AppAuth
) : ViewModel() {

    fun items(group: Byte) = repository.items(group).asLiveData(Dispatchers.Default)

    fun loadSubscriptions(group: Byte) = viewModelScope.launch {
        repository.getAll(group)
    }

    fun startCheckUpdates(group: Byte) {
        repository.startCheckUpdates(group)
    }

    fun stopCheckUpdates() {
        repository.stopCheckUpdates()
    }
}