package club.electro.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider


class MapYandexImpl(
    val onMapReady: (map: Map) -> Unit,
    val onFailure: (message: String) -> Unit
): Map {
    private lateinit var mapView: MapView

    private lateinit var mapObjects: MapObjectCollection
    private val markersList = mutableListOf<PlacemarkMapObject>()

    override fun init(view: Fragment?) {

    }

    override fun setView(view: View) {
        mapView = view as MapView
        mapObjects = mapView.map.mapObjects.addCollection()
        onMapReady(this)
    }

    override fun moveCamera(position: ECCameraPosition) {
        mapView.map.move(
            CameraPosition(Point(position.lat, position.lng), position.zoom, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )
    }

    override fun clear() {
        mapObjects.clear()
    }

    private val cliiistener: (mapObject: MapObject, clickListener: (it: ECMarker) -> Boolean) -> MapObjectTapListener = { mapObject, clickListener ->
//        println("Click on creator")
//        println(mapObject)
//        val ecMarker = (mapObject as PlacemarkMapObject).userData as ECMarker
//        println(ecMarker)
//        clickListener(ecMarker)
//        true

        MapObjectTapListener { mapObject, _ ->
            println("Click on setter")
            val ecMarker = (mapObject as PlacemarkMapObject).userData as ECMarker
            println(ecMarker)
            clickListener(ecMarker)
            true
        }
    }

//    private val mmlistener = MapObjectTapListener { mapObject, _ ->
//        println("Click on setter")
//        val ecMarker = (mapObject as PlacemarkMapObject).userData as ECMarker
//        println(ecMarker)
//        listener(ecMarker)
//        true
//    }

    override fun addMarker(ecMarker: ECMarker, clickListener: (it: ECMarker) -> Boolean, context: Context?): PlacemarkMapObject? {
        val mark = mapObjects.addPlacemark(Point(ecMarker.lat, ecMarker.lng))

        mark.setIcon(ImageProvider.fromResource(context, ecMarker.icon), IconStyle().setAnchor(PointF(0.5f, 1.0f)))

        mark.userData = ecMarker

        if (ecMarker.iconUrl != null && context != null) {
            mark.loadIcon(context, ecMarker.iconUrl)
        }

        mark.addTapListener(cliiistener(mark, clickListener))

        markersList.add(mark)

        return mark
    }

    override fun setOnCameraMoveListener(listener: () -> Unit) {

    }

    private lateinit var mlistener: GeoObjectTapListener

    override fun setOnMarkerClickListener(listener: (ecMarker: ECMarker) -> Boolean) {
        println("SetOnClickListener")
        mlistener = GeoObjectTapListener { mapObject ->
            println("Click on setter")
            val ecMarker = (mapObject as PlacemarkMapObject).userData as ECMarker
            println(ecMarker)
            listener(ecMarker)
            true
        }
        mapView.map.addTapListener(mlistener)
    }

    override fun cameraLat(): Double {
        return mapView.map.cameraPosition.target.latitude
    }

    override fun cameraLng(): Double {
        return mapView.map.cameraPosition.target.longitude
    }

    override fun cameraZoom(): Float {
        return mapView.map.cameraPosition.zoom
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


// https://stackoverflow.com/questions/63491864/kotlin-for-android-setting-a-google-maps-marker-image-to-a-url
fun PlacemarkMapObject.loadIcon(context: Context, url: String?) {
    val img = ImageView(context)
    // Чтобы вызвать RequestListener.onResourceReady на UI-потоке, необходимо построить запрос Glide с помощью метода into()
    // Для этого нужен фейковый ImageView
    // Если вызывать не на главном потоке, то setIcon() выдаст ошибку
    // https://bumptech.github.io/glide/javadocs/4120/index.html?com/bumptech/glide/request/RequestListener.html

    Glide.with(context)
        .asBitmap()
        .override(100, 100)
        .load(url)
        .timeout(5_000)
        //.error(R.drawable.map_group) // to show a default icon in case of any errors
        .listener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Bitmap?,
                model: Any?,
                target: Target<Bitmap>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                return resource?.let {
                    ImageProvider.fromBitmap(it)
                }?.let {
                    try {
                        setIcon(it)
                        true
                    } catch (e: Exception) {
                        // Маркер был удален с карты?
                        println("Marker removed")
                        false
                    }
                } ?: false
            }
        }).into(img)
}