package club.electro.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import club.electro.R
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
        // val sydney = LatLng(-34.0, 151.0)
        //googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(cameraPosition.lat, cameraPosition.lng), cameraPosition.zoom))

        viewModel.data.observe(viewLifecycleOwner) {
            val icon = BitmapDescriptorFactory.fromResource(R.drawable.socket);
            it.forEach {
                val marker = LatLng(it.lat, it.lng)
                googleMap.addMarker(MarkerOptions().position(marker).title("Marker").icon(icon))
            }
        }

        googleMap.setOnCameraMoveListener {
            viewModel.saveCameraState(MapCameraPosition(
                lat = googleMap.cameraPosition.target.latitude,
                lng = googleMap.cameraPosition.target.longitude,
                zoom = googleMap.cameraPosition.zoom,
            ))
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

