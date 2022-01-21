package club.electro.ui.map

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import club.electro.R
import club.electro.ui.map.socket.SocketFragment.Companion.socketId
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.graphics.Bitmap
import android.view.*
import club.electro.dto.*
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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment : Fragment() {
    private val viewModel: MapViewModel by viewModels (
        ownerProducer = ::requireParentFragment
    )

    private val callback = OnMapReadyCallback { googleMap ->
        viewModel.setFilter(MARKER_TYPE_GROUP, true)
        viewModel.getAllMarkers()

        val cameraPosition = viewModel.loadCameraState()

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(cameraPosition.lat, cameraPosition.lng), cameraPosition.zoom))

        viewModel.markers.observe(viewLifecycleOwner) { markersList ->
            googleMap.clear()

            val socketIcon = BitmapDescriptorFactory.fromResource(R.drawable.socket);
            val groupIcon = BitmapDescriptorFactory.fromResource(R.drawable.socket);

            markersList.forEach { marker ->
               val coords = LatLng(marker.lat, marker.lng)

               val mapMarker = when (marker.type) {
                    MARKER_TYPE_SOCKET -> googleMap.addMarker(MarkerOptions().position(coords).icon(socketIcon))
                    MARKER_TYPE_GROUP -> googleMap.addMarker(MarkerOptions().position(coords).icon(groupIcon))
                    else -> null
               }

               mapMarker?.let { it ->
                    it.tag = marker
                    marker.icon?.let { icon ->
                        mapMarker.loadIcon(requireContext(), icon)
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
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        viewModel.markersFilter.let { filter ->
            activity?.run {
                menuInflater.inflate(R.menu.menu_map, menu)
                menu.findItem(R.id.show_groups).setChecked(filter.contains(MARKER_TYPE_GROUP))
                menu.findItem(R.id.show_sockets).setChecked(filter.contains(MARKER_TYPE_SOCKET))
            }
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.show_groups -> {
                item.isChecked = !item.isChecked
                viewModel.setFilter(MARKER_TYPE_GROUP, item.isChecked)
                true
            }
            R.id.show_sockets -> {
                item.isChecked = !item.isChecked
                viewModel.setFilter(MARKER_TYPE_SOCKET, item.isChecked)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
        .timeout(5_000)
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

