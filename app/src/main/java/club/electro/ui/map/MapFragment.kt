package club.electro.ui.map

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import club.electro.R
import club.electro.dto.MARKER_TYPE_SOCKET
import club.electro.dto.MapMarker
import club.electro.ui.map.socket.SocketFragment.Companion.socketId
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import club.electro.dto.MARKER_TYPE_GROUP
import club.electro.repository.ThreadLoadTarget
import club.electro.ui.thread.ThreadFragment.Companion.postId
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.android.gms.maps.model.Marker
import com.bumptech.glide.request.target.Target
import java.net.URL


class MapFragment : Fragment() {
    private val viewModel: MapViewModel by viewModels (
        ownerProducer = ::requireParentFragment
    )

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        viewModel.getAllMarkers()

        val cameraPosition = viewModel.loadCameraState()

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(cameraPosition.lat, cameraPosition.lng), cameraPosition.zoom))

        viewModel.data.observe(viewLifecycleOwner) {
            val socketIcon = BitmapDescriptorFactory.fromResource(R.drawable.socket);
            val groupIcon = BitmapDescriptorFactory.fromResource(R.drawable.socket);

            it.forEach {
               val coords = LatLng(it.lat, it.lng)

               val marker = when (it.type) {
                    MARKER_TYPE_SOCKET -> googleMap.addMarker(MarkerOptions().position(coords).icon(socketIcon))
                    MARKER_TYPE_GROUP -> googleMap.addMarker(MarkerOptions().position(coords).icon(groupIcon))
                    else -> null
                }

                marker?.let { marker->
                    marker.tag = it
                    it.icon?.let { icon ->
                        println("Loadicon " + icon)
                        marker.loadIcon(requireContext(), icon)
                    }
                }
            }
        }

        googleMap.setOnCameraMoveListener {
            viewModel.saveCameraState(MapCameraPosition(
                lat = googleMap.cameraPosition.target.latitude,
                lng = googleMap.cameraPosition.target.longitude,
                zoom = googleMap.cameraPosition.zoom,
            ))
        }

        googleMap.setOnMarkerClickListener {
            val marker = it.tag as MapMarker
            when (marker.type) {
                MARKER_TYPE_SOCKET -> {
                    findNavController().navigate(
                        R.id.action_nav_map_to_socketFragment,
                        Bundle().apply {
                            socketId = marker.id
                        }
                    )
                }
                MARKER_TYPE_GROUP -> {
                    findNavController().navigate(
                        R.id.action_nav_map_to_threadFragment,
                        Bundle().apply {
                            threadType = marker.data!!.threadType!!
                            threadId = marker.data!!.threadId!!
                            postId = ThreadLoadTarget.TARGET_POSITION_FIRST_UNREAD
                        }
                    )
                }

            }
            true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}

data class MapCameraPosition (
    val lat: Double,
    val lng: Double,
    val zoom: Float
)

// https://stackoverflow.com/questions/63491864/kotlin-for-android-setting-a-google-maps-marker-image-to-a-url
fun Marker.loadIcon(context: Context, url: String?) {
    Glide.with(context)
        .asBitmap()
        .load(url)
        .error(R.drawable.socket) // to show a default icon in case of any errors
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
                    setIcon(it); true
                } ?: false
            }
        }).submit()
}

