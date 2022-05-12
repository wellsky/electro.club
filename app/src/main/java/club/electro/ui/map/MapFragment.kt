package club.electro.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import club.electro.MainViewModel
import club.electro.R
import club.electro.ToolBarConfig
import club.electro.dto.MARKER_TYPE_GROUP
import club.electro.dto.MARKER_TYPE_SOCKET
import club.electro.dto.MapMarkerData
import club.electro.repository.thread.ThreadLoadTarget
import club.electro.ui.map.socket.SocketFragment.Companion.socketId
import club.electro.ui.settings.SETTINGS_MAP_KEY
import club.electro.ui.settings.SETTINGS_MAP_VALUE_YANDEX
import club.electro.ui.thread.ThreadFragment.Companion.targetPostId
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MapFragment : Fragment(), GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private val viewModel: MapViewModel by viewModels()
    private var currentMarkerData: List<MapMarkerData> = emptyList()

    private lateinit var mapProvider: String
    private lateinit var map: Map

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().run {
            val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
            mainViewModel.updateActionBarConfig(ToolBarConfig(
                subtitle = "",
                onClick = {}
            ))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getAllMarkers()

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        mapProvider = prefs.getString(SETTINGS_MAP_KEY, SETTINGS_MAP_VALUE_YANDEX) ?: SETTINGS_MAP_VALUE_YANDEX

        MapsInitializer.initialize(requireContext())
    }

    private val markerClickListener: (it: ECMarker) -> Boolean = {
        val markerData = it.data
        println(markerData)
        when (markerData.type) {
            MARKER_TYPE_SOCKET -> {
                findNavController().navigate(
                    R.id.action_nav_map_to_socketFragment,
                    Bundle().apply {
                        socketId = markerData.id
                    }
                )
            }

            MARKER_TYPE_GROUP -> {
                println("Click on group")
                findNavController().navigate(
                    R.id.action_nav_map_to_threadFragment,
                    Bundle().apply {
                        threadType = markerData.data!!.threadType!!
                        threadId = markerData.data!!.threadId!!
                        targetPostId = ThreadLoadTarget.TARGET_POSITION_FIRST_UNREAD
                    }
                )
            }

        }
        true
    }

    private fun mapReadyCallback(map: Map) {
        val cameraPosition = viewModel.loadCameraState()
        map.moveCamera(cameraPosition)

        println("callback running")

        viewModel.markers.observe(viewLifecycleOwner) { markersList ->
            println("markers observed")
            if (currentMarkerData != markersList) {
                println("markers observed are different")
                currentMarkerData = markersList
                map.clear()

                val socketIcon = R.drawable.map_socket
                val groupIcon = R.drawable.map_group

                val context = requireContext()

                markersList.forEach { item ->
                    val ecMarker = when (item.type) {
                        MARKER_TYPE_SOCKET -> map.addMarker(
                            ECMarker(
                                lat = item.lat,
                                lng = item.lng,
                                icon = socketIcon,
                                data = item,
                                iconUrl = item.icon
                            ),
                            clickListener = markerClickListener,
                            context = context,
                        )
                        MARKER_TYPE_GROUP -> map.addMarker(
                            ECMarker(
                                lat = item.lat,
                                lng = item.lng,
                                icon = groupIcon,
                                data = item,
                                iconUrl = item.icon
                            ),
                            clickListener = markerClickListener,
                            context = context,
                        )
                        else -> null
                    }
                }
            }
        }

        map.setOnCameraMoveListener {
            try {
                println("save coords")
                viewModel.saveCameraState(
                    ECCameraPosition(
                        lat = map.cameraLat(),
                        lng = map.cameraLng(),
                        zoom = map.cameraZoom()
                    )
                )
                println("save coords: lat " + map.cameraLat())
            } catch (e: Exception) {
                println("saveCameraState exception")
            }
        }

        map.setOnMarkerClickListener {
            markerClickListener(it)
        }

        enableMyLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        return when (mapProvider) {
            SETTINGS_MAP_VALUE_YANDEX -> inflater.inflate(R.layout.fragment_map_yandex, container, false)
            else -> inflater.inflate(R.layout.fragment_map_google, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (mapProvider == SETTINGS_MAP_VALUE_YANDEX) {
            map = MapYandexImpl(
                onMapReady = { mapReadyCallback(map) },
                onFailure = { message ->
                    Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                        .show()
                }
            )

            map.setView(requireView().findViewById(R.id.yandex_map))
        } else {
            map = MapGoogleImpl(
                onMapReady = { mapReadyCallback(map) },
                onFailure = { message ->
                    Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                        .show()
                }
            )

            map.init(
                view = childFragmentManager.findFragmentById(R.id.google_map),
            )
        }
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

    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationMode(true, requireContext())
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(
                requireActivity() as AppCompatActivity, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        }
        // [END maps_check_location_permission]
    }

    // [START maps_check_location_permission_result]
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (PermissionUtils.isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            // permissionDenied = true
            // [END_EXCLUDE]
        }
    }

    override fun onStart() {
        super.onStart()
        map.onStart()
    }

    override fun onStop() {
        super.onStop()
        map.onStop()
    }

    override fun onPause() {
        super.onPause()
        if (map.destroyObjectsOnPause()) {
            currentMarkerData = emptyList()
        }
    }

    override fun onResume() {
        super.onResume()
        map.onStart()
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onMyLocationButtonClick(): Boolean {
        TODO("Not yet implemented")
    }

    override fun onMyLocationClick(p0: Location) {
        TODO("Not yet implemented")
    }
}




