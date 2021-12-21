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

    private val prefs = application.getSharedPreferences("map", Context.MODE_PRIVATE)

    private val latKey = "lat"
    private val lngKey = "lng"
    private val zoomKey = "zoom"

    fun getAllMarkers() = viewModelScope.launch {
        repository.getAll()
    }

    fun saveCameraState(lat: Double, lng: Double, zoom: Float) {
        with(prefs.edit()) {
            putDouble(latKey, lat)
            putDouble(lngKey, lng)
            putFloat(zoomKey, zoom)
            apply()
        }
    }

    fun loadCameraState(): LatLng {
        val lat = prefs.getDouble(latKey, 0.0)
        val lng = prefs.getDouble(lngKey, 0.0)
        return LatLng(lat, lng)
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