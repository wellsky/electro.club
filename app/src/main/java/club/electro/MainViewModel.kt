package club.electro

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import club.electro.api.ApiService
import club.electro.auth.AppAuth
import club.electro.model.NetworkStatus
import club.electro.repository.attachments.AttachmentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.*
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
    val attachmentsRepository: AttachmentsRepository,
): ViewModel() {
    private val _config = MutableLiveData<ToolBarConfig>()
    val config: LiveData<ToolBarConfig>
        get() = _config

    fun updateActionBarConfig(config: ToolBarConfig) = _config.postValue(config)

    suspend fun uploaderJob() {
        attachmentsRepository.uploaderJob()
    }

}

data class ToolBarConfig (
    val title: String? = null,
    val subtitle: String? = null,
    val scroll: Boolean = false,
    val onClick: () -> Unit = {}
)