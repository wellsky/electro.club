package club.electro.ui.map

import android.content.Context
import androidx.fragment.app.Fragment
import club.electro.dto.MapMarkerData
import com.google.android.gms.maps.model.BitmapDescriptor

interface Map {
    fun init(
        view: Fragment?
    )

    fun moveCamera(position: ECCameraPosition)
    fun clear()
    fun addMarker(marker: ECMarker, context: Context? = null)
    fun setOnCameraMoveListener(listener: () -> Unit)
    fun setOnMarkerClickListener(listener: (ecMarker: ECMarker) -> Boolean)

    fun cameraLat(): Double
    fun cameraLng(): Double
    fun cameraZoom(): Float
}

data class ECMarker (
    val lat: Double,
    val lng: Double,
    val icon: BitmapDescriptor,
    val data: MapMarkerData,

    val iconUrl: String? = null,
)

data class ECCameraPosition (
    val lat: Double,
    val lng: Double,
    val zoom: Float
)