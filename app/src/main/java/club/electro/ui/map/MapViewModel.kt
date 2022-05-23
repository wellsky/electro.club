package club.electro.ui.map

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import club.electro.dto.MARKER_TYPE_GROUP
import club.electro.repository.map.MapRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repository: MapRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {
    val markers = repository.markers.asLiveData(Dispatchers.Default)

    val markersFilter: MutableList<Byte>  = mutableListOf()

    private val prefs = context.getSharedPreferences("map", Context.MODE_PRIVATE)

    private val latKey = "lat"
    private val lngKey = "lng"
    private val zoomKey = "zoom"
    private val filterKey = "filter"

    init {
        val filterJson = prefs.getString(filterKey, "[" + MARKER_TYPE_GROUP + "]")
        markersFilter.addAll(
            Gson().fromJson<MutableList<Byte>>(filterJson, object : TypeToken<MutableList<Byte>>() {}.type)
        )
        repository.setMarkersFilter(markersFilter)

        //locationProvider.installLocationUpdates()
        //locationProvider.startUpdates()
        startLocationListener()
    }

    fun startLocationListener() {
        locationProvider.addSubscriber("my_current_location") {
            println("LOCATION UPDATE")
            println(it.latitude)
        }
    }

    fun stopLocationListener() {
        locationProvider.removeSubscriber("my_current_location")
    }

    fun getAllMarkers() = viewModelScope.launch {
        repository.getAll()
    }

    // TODO можно ли во viewModel сохранять и загружать sharedPrefs с т.ч. чистой архитектуры?
    fun saveCameraState(position: ECCameraPosition) {
        with(prefs.edit()) {
            putDouble(latKey, position.lat)
            putDouble(lngKey, position.lng)
            putFloat(zoomKey, position.zoom)
            apply()
        }
    }

    fun loadCameraState(): ECCameraPosition {
        return ECCameraPosition(
            lat = prefs.getDouble(latKey, 0.0),
            lng = prefs.getDouble(lngKey, 0.0),
            zoom = prefs.getFloat(zoomKey, 0F),
        )
    }

    fun setFilter(value: Byte, show: Boolean) {
        if (show) markersFilter.add(value) else markersFilter.remove(value)
        repository.setMarkersFilter(markersFilter)
        with(prefs.edit()) {
            putString(
                filterKey,
                Gson().toJson(markersFilter, object : TypeToken<MutableList<Byte>>() {}.type)
            )
            apply()
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

