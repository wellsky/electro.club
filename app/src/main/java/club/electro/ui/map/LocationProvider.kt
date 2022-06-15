package club.electro.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationProvider @Inject constructor (
    @ApplicationContext val context: Context
) {
    /**
     * Если принимает значение true, значит провайдер нарвался на отсутствие разрешений и кто-то
     * в UI-слое должен создать запрос на получение разрешений
     * и после этого вызывать onPermissionsChanged()
     */
    val permissionsRequired = MutableStateFlow(false)

    /**
     * Ключ - любая строка, которая должна быть уникальной для каждого слушателя
     * Значение - коллбэк, который будет вызываться при изменении координат
     */
    private val subscribers = mutableMapOf<String, (location: Location) -> Unit>()

    /**
     * Проверяется при старте обновлений. Если true, то еще раз стартовать не надо.
     */
    private var updatesEnabled = false

    /**
     * Последняя полученная геопозиция
     */
    private var lastLocation: Location? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // globally declare LocationRequest
    private lateinit var locationRequest: LocationRequest

    // globally declare LocationCallback
    private lateinit var locationCallback: LocationCallback

    init {
        installLocationUpdates()
    }

    fun addSubscriber(name: String, callback: (location: Location) -> Unit) {
        subscribers[name] = callback
        startUpdates()
    }

    fun removeSubscriber(name: String) {
        subscribers.remove(name)
        if (subscribers.isEmpty()) {
            stopUpdates()
        }
    }

    /**
     * call this method in onCreate
     * onLocationResult call when location is changed
     */
    private fun installLocationUpdates()
    {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationRequest = LocationRequest()
        locationRequest.interval = 500 // В миллисекундах
        locationRequest.fastestInterval = 500 // В миллисекундах
        locationRequest.smallestDisplacement = 1f // В метрах
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY // set according to your app function
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()) {
                    val location = locationResult.lastLocation
                    lastLocation = location
                    if (location != null) {
                        subscribers.forEach {
                            it.value.invoke(location)
                        }
                    }
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            CoroutineScope(Dispatchers.Default).launch {
                permissionsRequired.emit(true)
            }
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    fun getLastLocation(): Location? {
        return lastLocation
    }

    fun onPermissionsChanged() {
        startUpdates()
    }

    // stop receiving location update when activity not visible/foreground
    fun stopUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        updatesEnabled = false
    }

    // start receiving location update when activity  visible/foreground
    fun startUpdates() {
        if (!updatesEnabled) {
            startLocationUpdates()
        }
        updatesEnabled = true
    }
}

/**
 * Permissions utils:
 * https://gist.github.com/tatocaster/b4a5c5834208b94e7548295b6c98b187
 *
 */