package club.electro

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import club.electro.auth.AppAuth
import club.electro.model.NetworkStatus
import club.electro.repository.attachments.AttachmentsRepository
import club.electro.ui.map.LocationProvider
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val appAuth: AppAuth,
    val networkStatus : NetworkStatus,
    val attachmentsRepository: AttachmentsRepository,
    private val locationProvider: LocationProvider,
): ViewModel() {
    val requireLocationPermission = locationProvider.permissionsRequired.asLiveData(Dispatchers.Default)
    val resources: Resources = context.resources

    private val toolBarConfig = MutableStateFlow(ToolBarConfig())

    private val _state = combine(
        appAuth.authState,
        toolBarConfig,
        networkStatus.status,
    ) { auth, toolBarConfig, networkStatus ->
        val menuHeaderConfig =
            if (auth.authorized) {
                MenuHeaderConfig(
                    imageUrl = auth.avatar,
                    title = auth.name,
                    subTitle = auth.transportName
                        ?: resources.getString(R.string.transport_not_set)
                )
            } else {
                MenuHeaderConfig(
                    title = resources.getString(R.string.nav_header_title),
                    subTitle = resources.getString(R.string.nav_header_subtitle)
                )
            }

        val statusString = when (networkStatus) {
            NetworkStatus.Status.OFFLINE -> resources.getString(R.string.network_status_offline)
            NetworkStatus.Status.ERROR -> resources.getString(R.string.network_status_error)
            else -> null
        }

        val toolBarWithNetworkConfig = if (statusString != null) {
            toolBarConfig.copy ( subtitle = statusString )
        } else {
            toolBarConfig
        }

        MainViewState(
            toolBar = toolBarWithNetworkConfig,
            menuHeader = menuHeaderConfig,
        )
    }

    //MutableLiveData<MainViewState>()
    val state: LiveData<MainViewState>
        get() = _state.asLiveData(Dispatchers.Default)

    fun updateActionBarConfig(config: ToolBarConfig) = viewModelScope.launch {
        toolBarConfig.emit(config)
    }

    fun updateLocationPermissions() {
        locationProvider.onPermissionsChanged()
    }

    suspend fun uploaderJob() {
        attachmentsRepository.uploaderJob()
    }
}
