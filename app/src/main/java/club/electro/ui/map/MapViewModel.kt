package club.electro.ui.map

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import androidx.paging.cachedIn
import club.electro.repository.map.MapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repository: MapRepository
) : ViewModel() {
    val markers = repository.markers.asLiveData(Dispatchers.Default)

    val markersFilter: MutableList<Byte>  = mutableListOf()

    private val prefs = context.getSharedPreferences("map", Context.MODE_PRIVATE)

    private val latKey = "lat"
    private val lngKey = "lng"
    private val zoomKey = "zoom"

    fun getAllMarkers() = viewModelScope.launch {
        repository.getAll()
    }

    // TODO можно ли во viewModel сохранять и загружать sharedPrefs с т.ч. чистой архитектуры?
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

    fun setFilter(value: Byte, show: Boolean) {
        if (show) markersFilter.add(value) else markersFilter.remove(value)
        repository.setMerkersFilter(markersFilter)
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