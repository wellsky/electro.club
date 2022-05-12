package club.electro.ui.map

import android.content.Context
import android.view.View
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import club.electro.dto.MapMarkerData
import com.google.android.gms.maps.model.BitmapDescriptor
import com.yandex.mapkit.map.PlacemarkMapObject

interface Map {
    // Если какой-то провайдер потребует инициализацию до раздувания фрагмента,
    // То понадобится раделить init на 2 части
    // fun initBeforeInflate(context: Context)
    // fun initAfterInflate(view: Fragment?)
    fun init(view: Fragment?)

    fun setView(view: View)

    fun moveCamera(position: ECCameraPosition)
    fun clear()
    fun addMarker(ecMarker: ECMarker, clickListener: (it: ECMarker) -> Boolean = { true }, context: Context? = null)
    fun setOnCameraMoveListener(listener: () -> Unit)
    fun setOnMarkerClickListener(listener: (ecMarker: ECMarker) -> Boolean)

    fun cameraLat(): Double
    fun cameraLng(): Double
    fun cameraZoom(): Float

    fun onStart()
    fun onStop()

    fun setMyLocationMode(enabled: Boolean, context: Context)

    /**
     * Если провайдер карт удаляет свои маркеры на паузе фрагмента, то метод возвращает true
     */
    fun destroyObjectsOnPause(): Boolean
}

data class ECMarker (
    val lat: Double,
    val lng: Double,
    @DrawableRes
    val icon: Int,
    val data: MapMarkerData,

    val iconUrl: String? = null,
)

data class ECCameraPosition (
    val lat: Double,
    val lng: Double,
    val zoom: Float
)