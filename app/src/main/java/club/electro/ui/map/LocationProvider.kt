package club.electro.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class LocationProvider @Inject constructor (
    @ApplicationContext val context: Context
) {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // declare a global variable FusedLocationProviderClient
    // private lateinit var fusedLocationClient: FusedLocationProviderClient

    // in onCreate() initialize FusedLocationProviderClient
    //fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)


    // globally declare LocationRequest
    private lateinit var locationRequest: LocationRequest

    // globally declare LocationCallback
    private lateinit var locationCallback: LocationCallback


    /**
     * call this method in onCreate
     * onLocationResult call when location is changed
     */
    fun getLocationUpdates()
    {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationRequest = LocationRequest()
        locationRequest.interval = 500
        locationRequest.fastestInterval = 500
        locationRequest.smallestDisplacement = 170f // 170 m = 0.1 mile
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                if (locationResult.locations.isNotEmpty()) {
                    // get latest location
                    val location =
                        locationResult.lastLocation
                    // use your location object
                    // get latitude , longitude and other info from this
                    println("LOCATION UPDATE")
                    println(location.latitude)
                }


            }
        }
    }

    //start location updates
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
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    // stop location updates
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // stop receiving location update when activity not visible/foreground
    fun stopUpdates() {
        stopLocationUpdates()
    }

    // start receiving location update when activity  visible/foreground
    fun startUpdates() {
        startLocationUpdates()
    }
}