package com.tasa

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.tasa.domain.UserInfoRepository
import com.tasa.newlocation.UserActivityTransitionManager
import com.tasa.ui.screens.newLocation.TasaLocation
import com.tasa.ui.screens.newLocation.toTasaLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.util.GeoPoint

/**
 *  1º Fill array with the last 10 locations.
 *  2º Calculate the average location.
 *  3º Calculate the average accuracy.
 *  4º Remo
 *
 */
private const val MAX_LOCATION_HISTORY = 10

private const val MAX_DRIFTED_METERS = 3f

class LocationManager(
    private val context: Context,
    private val activityRecognitionManager: UserActivityTransitionManager,
    private val locationClient: FusedLocationProviderClient,
    private val userInfo: UserInfoRepository,
) {
    private val lastLocations = ArrayList<Location>(MAX_LOCATION_HISTORY)
    private val discardedLocations = ArrayList<Location>(3)

    init {

    }
    private var updates = 0
    private val averageAccuracy: Float
        get() {
            if (lastLocations.isEmpty()) return 0f
            return lastLocations.map { it.accuracy }.average().toFloat()
        }

    private var userActivity: String? = null

    private fun lowestAccuracyIndex(): Int {
        return lastLocations.indexOf(lastLocations.maxBy { it.accuracy })
    }

    private fun mostFurtherLocationIndex() : Int {
        val dist = lastLocations.map { it.distanceTo(calculatedCentralPoint) }
        return dist.indexOf(dist.max())
    }



    private var locationCallback: LocationCallback? = null


    private var locationRequest =
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3.seconds.inWholeMilliseconds,
        ).build()

    /*private val calculatedCentralPoint: Location
        get() {
            val filteredList = lastLocations
            val avgLat = filteredList.map { it.latitude }.average()
            val avgLon = filteredList.map { it.longitude }.average()
            return Location("calculated").apply {
                latitude = avgLat
                longitude = avgLon
            }
        }*/
    private val calculatedCentralPoint: Location
        get() {
            val sumWeight = lastLocations.sumOf { 1.0 / it.accuracy.coerceAtLeast(1f) }
            val weightedLat = lastLocations.sumOf { (it.latitude / it.accuracy.coerceAtLeast(1f)) } / sumWeight
            val weightedLon = lastLocations.sumOf { (it.longitude / it.accuracy.coerceAtLeast(1f)) } / sumWeight
            return Location("weighted").apply {
                latitude = weightedLat
                longitude = weightedLon
            }
        }


    private val discardedLocationsCentralPoint: Location
        get() {
            val avgLat = discardedLocations.map { it.latitude }.average()
            val avgLon = discardedLocations.map { it.longitude }.average()
            return Location("discarded").apply {
                latitude = avgLat
                longitude = avgLon
            }
        }
    /*private val calculatedCentralPoint: Location
        get() {
            val sumWeight = lastLocations.sumOf { 1.0 / it.accuracy }
            val weightedLat = lastLocations.sumOf { (it.latitude / it.accuracy) } / sumWeight
            val weightedLon = lastLocations.sumOf { (it.longitude / it.accuracy) } / sumWeight
            return Location("weighted").apply {
                latitude = weightedLat
                longitude = weightedLon
            }
        }
*/

    private val _centralLocationFlow = MutableStateFlow<TasaLocation>(TasaLocation(
        point = GeoPoint(0.0, 0.0),
        accuracy = 0f,
        altitude = null,
        time = null,
        updates = 0
    ))
    val centralLocationFlow: StateFlow<TasaLocation> = _centralLocationFlow.asStateFlow()


    private fun saveLocation(location: Location) {
       if (lastLocations.size == MAX_LOCATION_HISTORY) {
            val index = lowestAccuracyIndex()
            lastLocations[index] = location
        } else {
            lastLocations.add(location)
        }
        updates++
        Log.d("LocationManagerMine", "Location saved: ${location.latitude}, ${location.longitude} with accuracy ${location.accuracy}")
        _centralLocationFlow.value = TasaLocation(
            point = GeoPoint(calculatedCentralPoint.latitude,
                calculatedCentralPoint.longitude),
            accuracy = averageAccuracy,
            altitude = null,
            time = null,
            updates = updates
        )
    }


    private fun validateLocation(location: Location): Boolean {
        if (lastLocations.size < MAX_LOCATION_HISTORY) return true
        //discard locations that are too far from the calculated central point
        val locCalc = location.distanceTo(calculatedCentralPoint)
        Log.d("LocationManagerMine", "Location distance to central point: $locCalc meters")
            if (locCalc >= MAX_DRIFTED_METERS) {
            return when (userActivity) {
                "IN_VEHICLE", "ON_BICYCLE", "ON_FOOT", "WALKING", "RUNNING", "UNKNOWN" -> true
                "STILL", "TILTING" -> {
                    if (location.accuracy > averageAccuracy) return false
                    if (location.distanceTo(discardedLocationsCentralPoint) >= MAX_DRIFTED_METERS) {
                        discardedLocations.clear()
                        true
                    } else false
                }
                else -> false
            }
        }
        discardedLocations.clear()
        return true
    }

    @RequiresPermission(allOf =
        [Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION])
    fun startUp(){
        CoroutineScope(Dispatchers.IO).launch{
            startListeningForActivityTransitions()
            startLocationUpdates(locationRequest)
            startListeningForActivity()
        }
    }


    @RequiresPermission(allOf =
        [Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startLocationUpdates(locationRequest: LocationRequest) {
        locationCallback =
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    for (location in result.locations) {
                       if (validateLocation(location = location)){
                           saveLocation(location)
                       } else {
                           discardedLocations.add(location)
                       }
                    }
                }
            }
        locationCallback?.let {
            locationClient.requestLocationUpdates(
                locationRequest,
                it,
                Looper.getMainLooper(),
            )
        }
    }


    @RequiresPermission(Manifest.permission.ACTIVITY_RECOGNITION)
    private suspend fun startListeningForActivityTransitions() {
        val result = activityRecognitionManager.registerActivityTransitions()
        if (result.isFailure) {
            // handle failure
            return
        }
    }

    private suspend fun startListeningForActivity() {
        userInfo.lastActivity.collect { activity ->
            userActivity =
                UserActivityTransitionManager.Companion.getActivityType(activity)
        }
    }
}
