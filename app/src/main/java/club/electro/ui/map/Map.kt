package club.electro.ui.map

import android.view.View
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import club.electro.dto.MapMarkerData
import club.electro.utils.getRandomString

interface Map {
    // Если какой-то провайдер потребует инициализацию до раздувания фрагмента,
    // То понадобится раделить init на 2 части
    // fun initBeforeInflate(context: Context)
    // fun initAfterInflate(view: Fragment?)
    fun init(view: Fragment?)

    fun setView(view: View)

    fun moveCamera(position: CameraPosition, smooth: Boolean = false)
    fun clear()

    fun addMarker(mapMarker: MapMarker, clickListener: (it: MapMarker) -> Boolean = { true })
    fun setMarkerPosition(mapMarker: MapMarker, lat: Double, lng: Double)

    fun setOnCameraMoveListener(listener: () -> Unit)
    fun setOnMarkerClickListener(listener: (mapMarker: MapMarker) -> Boolean)

    fun cameraLat(): Double
    fun cameraLng(): Double
    fun cameraZoom(): Float

    fun onStart()
    fun onStop()

    fun setMyLocationMode(enabled: Boolean)

    /**
     * Если провайдер карт удаляет свои маркеры на паузе фрагмента, то метод возвращает true
     */
    fun destroyObjectsOnPause(): Boolean
}


/**
 * Объект
 */
data class MapMarker (
    val id: String = getRandomString(16),

    var lat: Double,
    var lng: Double,
    @DrawableRes
    val icon: Int,

    val data: MapMarkerData? = null,
    val iconUrl: String? = null,
)

data class CameraPosition (
    val lat: Double,
    val lng: Double,
    val zoom: Float
)

