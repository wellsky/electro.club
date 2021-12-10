package club.electro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import club.electro.di.DependencyContainer

class MainViewModel: ViewModel() {
    private val _title = MutableLiveData<ToolBarConfig>()
    val title: LiveData<ToolBarConfig>
        get() = _title
    fun updateActionBarTitle(title: ToolBarConfig) = _title.postValue(title)

    val networkStatus = DependencyContainer.getInstance().networkStatus
}

data class ToolBarConfig (
    val title1: String,
    val title2: String? = null,
    val onClick: () -> Unit = {}
)