package club.electro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import club.electro.api.ApiService
import club.electro.auth.AppAuth
import club.electro.model.NetworkStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import javax.inject.Inject
import okhttp3.RequestBody

import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.http.Part
import java.io.File


@HiltViewModel
class MainViewModel @Inject constructor(
    val appAuth: AppAuth,
    val networkStatus : NetworkStatus,
    val apiService: ApiService,
): ViewModel() {
    private val _config = MutableLiveData<ToolBarConfig>()
    val config: LiveData<ToolBarConfig>
        get() = _config
    fun updateActionBarConfig(config: ToolBarConfig) = _config.postValue(config)

    init {
        viewModelScope.launch {
            val file = File("/storage/emulated/0/DCIM/Camera/IMG_20220213_130617.jpg")
            apiService.uploadPostDraftAttachment(
                file = MultipartBody.Part.createFormData(
                        "file",
                        file.name,
                        file.asRequestBody("image/*".toMediaType())
                )
            )
        }
    }
}

data class ToolBarConfig (
    val title: String? = null,
    val subtitle: String? = null,
    val onClick: () -> Unit = {}
)