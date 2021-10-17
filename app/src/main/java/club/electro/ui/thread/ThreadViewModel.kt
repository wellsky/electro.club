package club.electro.ui.thread

import android.app.Application
import androidx.lifecycle.*
import club.electro.repository.SubscriptionsRepository
import club.electro.repository.SubscriptionsRepositoryServerImpl
import kotlinx.coroutines.Dispatchers

class ThreadViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SubscriptionsRepository = SubscriptionsRepositoryServerImpl(application)
    val data = repository.data.asLiveData(Dispatchers.Default)

}