package club.electro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import club.electro.auth.AppAuth
import club.electro.model.NetworkStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val appAuth: AppAuth,
    val networkStatus : NetworkStatus
): ViewModel() {
    private val _config = MutableLiveData<ToolBarConfig>()
    val config: LiveData<ToolBarConfig>
        get() = _config
    fun updateActionBarConfig(config: ToolBarConfig) = _config.postValue(config)
}

data class ToolBarConfig (
    val title: String? = null,
    val subtitle: String? = null,
    val onClick: () -> Unit = {}
)