package club.electro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import club.electro.di.DependencyContainer
import club.electro.model.NetworkStatus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

class MainViewModel: ViewModel() {
    private val _title = MutableLiveData<ToolBarConfig>()
    val title: LiveData<ToolBarConfig>
        get() = _title
    fun updateActionBarTitle(title: ToolBarConfig) = _title.postValue(title)

    @Inject
    lateinit var networkStatus : NetworkStatus
}

data class ToolBarConfig (
    val title1: String,
    val title2: String? = null,
    val onClick: () -> Unit = {}
)