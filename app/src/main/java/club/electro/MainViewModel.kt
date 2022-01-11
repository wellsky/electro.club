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
    private val _title = MutableLiveData<ToolBarConfig>()
    val title: LiveData<ToolBarConfig>
        get() = _title
    fun updateActionBarTitle(title: ToolBarConfig) = _title.postValue(title)
}

data class ToolBarConfig (
    val title1: String,
    val title2: String? = null,
    val onClick: () -> Unit = {}
)