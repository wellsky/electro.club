package club.electro.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Marker

class MapGoogleImpl(
    val onMapReady: (map: Map) -> Unit,
    val onFailure: (message: String) -> Unit
): Map {
    private lateinit var googleMap: GoogleMap

    override fun init (
        view: Fragment?,
    ) {
        val mapFragment = view as SupportMapFragment?
        try {
            mapFragment?.getMapAsync(callback)
        } catch (e :Exception) {
            onFailure(e.message.toString())
        }
    }

    override fun setView(view: View) {

    }

    override fun moveCamera(position: ECCameraPosition) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(position.lat, position.lng), position.zoom))
    }

    override fun clear() {
        googleMap.clear()
    }

    private val callback = OnMapReadyCallback {
        googleMap = it
        onMapReady(this)
    }

    override fun addMarker(ecMarker: ECMarker, context: Context?) {
        val position = LatLng(ecMarker.lat, ecMarker.lng)

        val gmMarker = googleMap.addMarker(
            MarkerOptions().position(position).icon(ecMarker.icon)
        )

        if (gmMarker != null) {
            gmMarker.tag = ecMarker

            if (ecMarker.iconUrl != null && context != null) {
                gmMarker.loadIcon(context, ecMarker.iconUrl)
            }
        }
    }

    override fun setOnCameraMoveListener(listener: () -> Unit) {
        googleMap.setOnCameraMoveListener(listener)
    }

    override fun setOnMarkerClickListener(listener: (ecMarker: ECMarker) -> Boolean) {
        googleMap.setOnMarkerClickListener {
            listener(it.tag as ECMarker)
        }
    }

    override fun cameraLat() = googleMap.cameraPosition.target.latitude
    override fun cameraLng() = googleMap.cameraPosition.target.longitude
    override fun cameraZoom() = googleMap.cameraPosition.zoom

    override fun onStart() {

    }

    override fun onStop() {

    }

}



// https://stackoverflow.com/questions/63491864/kotlin-for-android-setting-a-google-maps-marker-image-to-a-url
fun Marker.loadIcon(context: Context, url: String?) {
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
                    BitmapDescriptorFactory.fromBitmap(it)
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