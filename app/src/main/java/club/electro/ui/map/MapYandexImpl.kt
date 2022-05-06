package club.electro.ui.map

import android.R
import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView


class MapYandexImpl(
    val onMapReady: (map: Map) -> Unit,
    val onFailure: (message: String) -> Unit
): Map {
    private lateinit var mapView: MapView

    override fun init(view: Fragment?) {
        TODO("Not yet implemented")
    }

    override fun setView(view: View) {
        mapView = view as MapView
    }

    override fun moveCamera(position: ECCameraPosition) {
        mapView.map.move(
            CameraPosition(Point(position.lat, position.lng), position.zoom, 0.0f, 0.0f)
//            Animation(Animation.Type.SMOOTH, 0),
//            null
        )
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun addMarker(marker: ECMarker, context: Context?) {
        TODO("Not yet implemented")
    }

    override fun setOnCameraMoveListener(listener: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun setOnMarkerClickListener(listener: (ecMarker: ECMarker) -> Boolean) {
        TODO("Not yet implemented")
    }

    override fun cameraLat(): Double {
        TODO("Not yet implemented")
    }

    override fun cameraLng(): Double {
        TODO("Not yet implemented")
    }

    override fun cameraZoom(): Float {
        TODO("Not yet implemented")
    }

    override fun onStart() {
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
    }

}