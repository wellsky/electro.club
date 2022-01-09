package club.electro.ui.subscriptions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import club.electro.application.ElectroClubApp
import club.electro.repository.SubscriptionsRepository
import club.electro.repository.SubscriptionsRepositoryServerImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SubscriptionsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SubscriptionsRepository = SubscriptionsRepositoryServerImpl()

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