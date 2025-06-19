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
import kotlin.time.Duration.Companion.milliseconds

/**
 *  1º Fill array with the last 10 locations.
 *  2º Calculate the average location.
 *  3º Calculate the average accuracy.
 *  4º Remo
 *
 */

//
private const val MAX_LOCATION_HISTORY = 10

private const val MAX_DRIFTED_METERS = 3f

class LocationManager(
    private val activityRecognitionManager: UserActivityTransitionManager,
    private val locationClient: FusedLocationProviderClient,
    private val userInfo: UserInfoRepository,
) {
    private val lastLocations = ArrayList<Location>(MAX_LOCATION_HISTORY)
    private val discardedLocations = ArrayList<Location>(3)

    private var updates = 0
    private val averageAccuracy: Float
        get() {
            if (lastLocations.isEmpty()) return 0f
            return lastLocations.map { it.accuracy }.average().toFloat()
        }

    private val precision: Float
        get() = lastLocations.calculatePrecision()

    private var userActivity: String? = null

    private var locationCallback: LocationCallback =
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    Log.d("LocationManagerMine", "Received: ${location.latitude}, ${location.longitude} with accuracy ${location.accuracy}")
                    if(!location.hasSpeed() && !location.hasAltitude()) continue
                    val (isValid, onDiscarded) = validateLocation(location)
                    Log.d("LocationManagerMine", "Location valid: $isValid, onDiscarded: $onDiscarded")
                    if (isValid){
                        saveLocation(location,onDiscarded)
                    } else {
                        discardedLocations.add(location)
                    }
                }
            }
        }


    private fun lowestAccuracyIndex(): Int {
        return lastLocations.indexOf(lastLocations.maxBy { it.accuracy })
    }

    private fun mostFurtherLocationIndex() : Int {
        val dist = lastLocations.map { it.distanceTo(calculatedCentralPoint) }
        return dist.indexOf(dist.max())
    }

    private fun ArrayList<Location>.centralPoint(): Location {
        if (this.isEmpty()) return Location("central")
        val avgLat = this.map { it.latitude }.average()
        val avgLon = this.map { it.longitude }.average()
        return Location("central").apply {
            latitude = avgLat
            longitude = avgLon
        }
    }

    //Check if the locations are all a radius of n
    private fun ArrayList<Location>.isAllInRadius(radius: Float): Boolean {
        if (this.isEmpty()) return false
        val centralPoint = this.centralPoint()
        return this.all { it.distanceTo(centralPoint) <= radius }
    }

    private val calculatedCentralPoint: Location
        get() {
            return lastLocations.centralPoint()
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

    private val _centralLocationFlow = MutableStateFlow<TasaLocation>(TasaLocation(
        point = GeoPoint(0.0, 0.0),
        accuracy = 0f,
        altitude = null,
        time = null,
        updates = 0
    ))
    val centralLocationFlow: StateFlow<TasaLocation> = _centralLocationFlow.asStateFlow()


    private fun saveLocation(location: Location, onDiscarded: Boolean = false) {
        if (lastLocations.size >= MAX_LOCATION_HISTORY) {
            if (userActivity == "STILL" && !onDiscarded) {
                val index = mostFurtherLocationIndex()
                lastLocations[index] = location
            } else {
                val index = lowestAccuracyIndex()
                lastLocations[index] = location
            }
        } else {
            lastLocations.add(location)
        }
        Log.d("LocationManagerMine", "Locations size: ${lastLocations.size}, Discarded size: ${discardedLocations.size}")
        //}
        updates++
        //Log.d("LocationManagerMine", "Location saved: ${location.latitude}, ${location.longitude} with accuracy ${location.accuracy}")
        _centralLocationFlow.value = TasaLocation(
            point = GeoPoint(calculatedCentralPoint.latitude,
                calculatedCentralPoint.longitude),
            accuracy = averageAccuracy,
            altitude = null,
            time = null,
            updates = updates
        )
    }


    private fun validateLocation(location: Location): Pair<Boolean, Boolean > {
        if (lastLocations.size < MAX_LOCATION_HISTORY) return true to false
        val locCalc = location.distanceTo(calculatedCentralPoint)
        if (locCalc >= MAX_DRIFTED_METERS) {
            return when (userActivity) {
                "IN_VEHICLE", "ON_BICYCLE", "ON_FOOT", "WALKING", "RUNNING", "UNKNOWN" -> true
                "STILL", "TILTING" -> {
                    //if (location.accuracy > averageAccuracy) false to false
                    // if precision is too high, discard the location
                    if(precision < MAX_DRIFTED_METERS) false to false
                    if (location.distanceTo(calculatedCentralPoint) <= MAX_DRIFTED_METERS) {
                        discardedLocations.clear()
                        true to true
                    } else false to false
                }
                else -> false to false
            } as Pair<Boolean, Boolean>
        }
        discardedLocations.clear()
        return true to false
    }

    @RequiresPermission(allOf =
        [Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION])
    fun startUp(){
        CoroutineScope(Dispatchers.IO).launch{
            startListeningForActivityTransitions()
            val locationRequest = createLocationRequest(
                interval = 10.milliseconds.inWholeMilliseconds,
                priority = Priority.PRIORITY_HIGH_ACCURACY
            )
            startLocationUpdates(locationRequest)
            startListeningForActivity()
        }
    }


    @RequiresPermission(allOf =
        [Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startLocationUpdates(locationRequest: LocationRequest) {
        locationCallback.let {
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

    @RequiresPermission(allOf =
        [Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION])
    private suspend fun startListeningForActivity() {
        userInfo.lastActivity.collect { activity ->
            userActivity =
                UserActivityTransitionManager.Companion.getActivityType(activity)
            if (updates >= 30){
                when (userActivity) {
                    "STILL", "TILTING" -> {
                        if (lastLocations.isNotEmpty() && lastLocations.isAllInRadius(MAX_DRIFTED_METERS)) {
                            stopLocationUpdates()
                        }
                    }
                    else -> {
                        startLocationUpdates(createLocationRequest(3.seconds.inWholeMilliseconds, Priority.PRIORITY_HIGH_ACCURACY))
                    }
                }
            }
        }
    }

    private fun createLocationRequest(interval: Long, priority: Int): LocationRequest {
        return LocationRequest.Builder(priority, interval)
            .setMinUpdateIntervalMillis(interval)
            .setWaitForAccurateLocation(false)
            .build()
    }

    private fun stopLocationUpdates() {
        locationCallback.let {
            locationClient.removeLocationUpdates(it)
            Log.d("LocationManagerMine", "Stopped location updates")
        }
    }

    private fun List<Location>.calculatePrecision(): Float {
        if (this.size < 2) return 0f
        return this.map { it.distanceTo(calculatedCentralPoint) }.average().toFloat()
    }

    private fun isPrecisionAcceptable(threshold: Float = 2.5f): Boolean {
        return lastLocations.calculatePrecision() <= threshold
    }

}


