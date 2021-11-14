package club.electro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _title = MutableLiveData<ToolBarDoubleTitle>()
    val title: LiveData<ToolBarDoubleTitle>
        get() = _title
    fun updateActionBarTitle(title: ToolBarDoubleTitle) = _title.postValue(title)
}

data class ToolBarDoubleTitle (
    val title1: String,
    val title2: String? = null
)