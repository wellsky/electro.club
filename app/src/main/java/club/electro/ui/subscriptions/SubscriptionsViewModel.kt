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
    val data = repository.data.asLiveData(Dispatchers.Default)

    fun loadSubscriptions(global: Boolean) = viewModelScope.launch {
        println("Load subscr, global = " + global)
        repository.getAll(global)
    }

    fun startCheckUpdates(global: Boolean) {
        println("Start subscr, global =  " + global)
        repository.startCheckUpdates(global)
    }

    fun stopCheckUpdates() {
        println("Stop subscr")
        repository.stopCheckUpdates()
    }
}