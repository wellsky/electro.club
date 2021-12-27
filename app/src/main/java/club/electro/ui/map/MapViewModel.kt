package club.electro.ui.map

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import club.electro.application.ElectroClubApp
import club.electro.repository.MapRepository
import club.electro.repository.MapRepositoryServerImpl
import club.electro.repository.ThreadRepository
import club.electro.repository.ThreadRepositoryServerImpl
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MapRepository = MapRepositoryServerImpl((application as ElectroClubApp).diContainer)
    val data = repository.data.asLiveData(Dispatchers.Default)

    val _mapFilter = MutableLiveData(MapFilter())
    val mapFilter: LiveData<MapFilter> = _mapFilter

    private val prefs = application.getSharedPreferences("map", Context.MODE_PRIVATE)

    private val latKey = "lat"
    private val lngKey = "lng"
    private val zoomKey = "zoom"

    fun getAllMarkers() = viewModelScope.launch {
        repository.getAll()
    }

    // TODO можно ли во viewModel сохранять и загружать sharedPrefs с т.ч. архитектуры?
    fun saveCameraState(position: MapCameraPosition) {
        with(prefs.edit()) {
            putDouble(latKey, position.lat)
            putDouble(lngKey, position.lng)
            putFloat(zoomKey, position.zoom)
            apply()
        }
    }

    fun loadCameraState(): MapCameraPosition {
        return MapCameraPosition(
            lat = prefs.getDouble(latKey, 0.0),
            lng = prefs.getDouble(lngKey, 0.0),
            zoom = prefs.getFloat(zoomKey, 0F),
        )
    }

    fun showGroups(show: Boolean) {
        _mapFilter.value?.let {
            _mapFilter.value =it.copy(showGroups = show)
        }
    }

    fun showSockets(show: Boolean) {
        _mapFilter.value?.let {
            _mapFilter.value =it.copy(showSockets = show)
        }
    }
}

fun SharedPreferences.Editor.putDouble(key: String?, value: Double): SharedPreferences.Editor? {
    return this.putLong(key, java.lang.Double.doubleToRawLongBits(value))
}

fun SharedPreferences.getDouble(key: String?, defaultValue: Double): Double {
    return if (!this.contains(key)) defaultValue else java.lang.Double.longBitsToDouble(
        this.getLong(
            key,
            0
        )
    )
}

data class MapFilter(
    val showGroups: Boolean = true,
    val showSockets: Boolean = false
)