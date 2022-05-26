package club.electro.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MapFragment : Fragment() {
    private val viewModel: MapViewModel by viewModels()

    /**
     * Сохраняет последние полученные маркеры, чтобы потом сравнивать их с новыми
     * Если новые те же самые, то пересоздавать не надо
     */
    private var currentMarkersList: List<MapMarkerData> = emptyList()

    /**
     * Маркер текущей позиции устройства
     */
    private var currentPositionMarker: MapMarker? = null

    /**
     * Строка из настроек
     */
    private lateinit var mapProvider: String

    /**
     * Имплементация карты
     */
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
    }

    private val markerClickListener: (it: MapMarker) -> Boolean = {
        it.data?.let { markerData ->
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
        }

        true
    }

    private fun mapReadyCallback(map: Map) {
        val cameraPosition = viewModel.loadCameraState()
        map.moveCamera(cameraPosition)

        println("callback running")

        viewModel.markers.observe(viewLifecycleOwner) { markersList ->
            println("markers observed")
            if (currentMarkersList != markersList) {
                println("markers observed are different")
                currentMarkersList = markersList
                map.clear()

                val socketIcon = R.drawable.map_socket
                val groupIcon = R.drawable.map_group

                markersList.forEach { item ->
                    val mapMarker = when (item.type) {
                        MARKER_TYPE_SOCKET -> map.addMarker(
                            MapMarker(
                                lat = item.lat,
                                lng = item.lng,
                                icon = socketIcon,
                                data = item,
                                iconUrl = item.icon
                            ),
                            clickListener = markerClickListener,
                        )
                        MARKER_TYPE_GROUP -> map.addMarker(
                            MapMarker(
                                lat = item.lat,
                                lng = item.lng,
                                icon = groupIcon,
                                data = item,
                                iconUrl = item.icon
                            ),
                            clickListener = markerClickListener,
                        )
                        else -> null
                    }
                }

                currentPositionMarker?.let {
                    println("create cur position marker")
                    createCurrentPositionMarker(
                        lat = it.lat,
                        lng = it.lng,
                    )
                }
            }
        }


        viewModel.currentLocation.observe(viewLifecycleOwner) { location ->
            currentPositionMarker?.let {
                println("OBSERVE LOCATION CHANGE POSITION")

                it.changePosition(location.latitude, location.longitude)
            } ?: run{
                println("OBSERVE LOCATION CREATE")

                createCurrentPositionMarker(
                    lat = location.latitude,
                    lng = location.longitude,
                )
            }
        }


        map.setOnCameraMoveListener {
            try {
                viewModel.saveCameraState(
                    CameraPosition(
                        lat = map.cameraLat(),
                        lng = map.cameraLng(),
                        zoom = map.cameraZoom()
                    )
                )
            } catch (e: Exception) {
                println("saveCameraState exception")
            }
        }

        map.setOnMarkerClickListener {
            markerClickListener(it)
        }
    }

    private fun createCurrentPositionMarker(lat: Double, lng: Double) {
        currentPositionMarker = MapMarker(
            lat = lat,
            lng = lng,
            icon = R.drawable.map_current_location
        )

        currentPositionMarker?.let {
            map.addMarker(it)
        }
    }

    private fun moveCameraToCurrentPositionMarker() {
        currentPositionMarker?.let {
            map.moveCamera(CameraPosition(it.lat, it.lng, 16f), true)
        }
    }

    private fun MapMarker.changePosition(lat: Double, lng: Double) {
        map.setMarkerPosition(this, lat, lng)
        this.lat = lat
        this.lng = lng
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
                },
                context = requireContext(),
            )

            map.setView(requireView().findViewById(R.id.yandex_map))
        } else {
            map = MapGoogleImpl(
                onMapReady = { mapReadyCallback(map) },
                onFailure = { message ->
                    Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                        .show()
                },
                context = requireContext(),
            )

            map.init(
                view = childFragmentManager.findFragmentById(R.id.google_map),
            )
        }

        view.findViewById<FloatingActionButton>(R.id.map_to_my_location_button)
            .setOnClickListener {
                moveCameraToCurrentPositionMarker()
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



    override fun onStart() {
        super.onStart()
        map.onStart()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopLocationListener()
        map.onStop()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopLocationListener()
        if (map.destroyObjectsOnPause()) {
            currentMarkersList = emptyList()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startLocationListener()
        map.onStart()
    }
}
