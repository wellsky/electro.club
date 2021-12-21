package club.electro.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import club.electro.R
import club.electro.dto.MapMarker
import club.electro.ui.map.socket.SocketFragment.Companion.socketId
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

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
            val icon = BitmapDescriptorFactory.fromResource(R.drawable.socket);
            it.forEach {
                val coords = LatLng(it.lat, it.lng)
                val marker = googleMap.addMarker(MarkerOptions().position(coords).title("Marker").icon(icon)) // TODO title
                marker?.tag = it

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
            findNavController().navigate(
                R.id.action_nav_map_to_socketFragment,
                Bundle().apply {
                    socketId = marker.id
                }
            )
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

