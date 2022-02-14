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
    @ApplicationContext val context: Context,
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
//            //val sourceFile = File("/storage/emulated/0/DCIM/Camera/IMG_20220213_130617.jpg")
//            val sourceFile = File("/storage/emulated/0/Download/86262-gorodskoj_rajon-gorodskoj_pejzazh-otrazhenie-gorizont-boston-3840x2160.jpg")
//
//            println("Uploading... " + sourceFile.name)
//
//            val outputDir = context.cacheDir // context being the Activity pointer
//            val destinationFile = File.createTempFile("temp_compressed", ".jpg", outputDir)
//            destinationFile.deleteOnExit()
//
//            val compressedImageFile = Compressor.compress(context, sourceFile) {
//                destination(destinationFile )
//                default(width = 1920, format = Bitmap.CompressFormat.JPEG, quality = 80)
//            }
//
//            apiService.uploadPostDraftAttachment(
//                file = MultipartBody.Part.createFormData(
//                        "file",
//                        compressedImageFile.name,
//                        compressedImageFile.asRequestBody("image/*".toMediaType())
//                )
//            )
        }
    }
}

data class ToolBarConfig (
    val title: String? = null,
    val subtitle: String? = null,
    val onClick: () -> Unit = {}
)