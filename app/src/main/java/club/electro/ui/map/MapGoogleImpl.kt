package club.electro.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import club.electro.ui.map.PermissionUtils.isPermissionGranted
import club.electro.ui.map.PermissionUtils.requestPermission
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
    private lateinit var map: GoogleMap

    private val callback = OnMapReadyCallback {
        map = it
        onMapReady(this)
    }

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

    override fun destroyObjectsOnPause() = false

    override fun setView(view: View) {

    }

    override fun moveCamera(position: ECCameraPosition) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(position.lat, position.lng), position.zoom))
    }

    override fun clear() {
        map.clear()
    }

    override fun addMarker(ecMarker: ECMarker, clickListener: (it: ECMarker) -> Boolean, context: Context?) {
        val position = LatLng(ecMarker.lat, ecMarker.lng)

        val gmMarker = map.addMarker(
            MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromResource(ecMarker.icon))
        )

        if (gmMarker != null) {
            gmMarker.tag = ecMarker

            if (ecMarker.iconUrl != null && context != null) {
                gmMarker.loadIcon(context, ecMarker.iconUrl)
            }
        }
    }

    override fun setOnCameraMoveListener(listener: () -> Unit) {
        map.setOnCameraMoveListener(listener)
    }

    override fun setOnMarkerClickListener(listener: (ecMarker: ECMarker) -> Boolean) {
        map.setOnMarkerClickListener {
            listener(it.tag as ECMarker)
        }
    }

    override fun setMyLocationMode(enabled: Boolean, context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            //map.isMyLocationEnabled = true
            map.isMyLocationEnabled = enabled
        }
    }

    override fun cameraLat() = map.cameraPosition.target.latitude
    override fun cameraLng() = map.cameraPosition.target.longitude
    override fun cameraZoom() = map.cameraPosition.zoom

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