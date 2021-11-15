package club.electro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
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