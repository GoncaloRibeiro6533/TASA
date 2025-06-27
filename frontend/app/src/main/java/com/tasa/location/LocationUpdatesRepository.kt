package com.tasa.location

import android.Manifest
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.tasa.activity.UserActivityTransitionManager
import com.tasa.domain.UserInfoRepository
import com.tasa.ui.screens.newLocation.TasaLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import kotlin.time.Duration.Companion.milliseconds

private const val MAX_LOCATION_HISTORY = 10
private const val MAX_LOCATION_HISTORY_DISCARDED = 5

private const val MAX_DRIFTED_METERS = 3f

class LocationUpdatesRepository(
    private val activityRecognitionManager: UserActivityTransitionManager,
    private val locationClient: FusedLocationProviderClient,
    private val userInfo: UserInfoRepository,
) {
    data class Area(val center: Location, val radius: Float)

    private val lastLocations = ArrayList<Location>(MAX_LOCATION_HISTORY)
    private val discardedLocations = ArrayList<Location>(MAX_LOCATION_HISTORY_DISCARDED)
    private val lastZone = ArrayList<Location>(MAX_LOCATION_HISTORY)

    private var possibleArea: Area? = null

    private var updates = 0
    private val averageAccuracy: Float
        get() {
            if (lastLocations.isEmpty()) return 0f
            return lastLocations.map { it.accuracy }.average().toFloat()
        }

    private val precision: Float
        get() = lastLocations.calculatePrecision()

    private var userActivity: String? = null

    private val locationFlow: MutableStateFlow<Location?> = MutableStateFlow(null)

    private var locationCallback: LocationCallback =
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    Log.d(
                        "LocationManagerMine",
                        "Location received: ${location.latitude}, ${location.longitude}, accuracy: ${location.accuracy}",
                    )
                    // if (!location.hasSpeed() && !location.hasAltitude()) continue
                    locationFlow.value = location
                }
            }
        }

    private fun lowestAccuracyIndex(): Int {
        return lastLocations.indexOf(lastLocations.maxBy { it.accuracy })
    }

    private fun mostFurtherLocationIndex(): Int {
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

    // Check if the locations are all a radius of n
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

    private val _centralLocationFlow =
        MutableStateFlow<TasaLocation?>(
            null,
        )
    val centralLocationFlow: StateFlow<TasaLocation?> = _centralLocationFlow.asStateFlow()

    private fun saveLocation(location: Location) {
        if (lastLocations.size == MAX_LOCATION_HISTORY) {
            // remove the oldest location
            lastLocations.removeAt(0)
            // add the new location
            lastLocations.add(location)
        } else {
            lastLocations.add(location)
        }
        _centralLocationFlow.value =
            TasaLocation(
                point =
                    GeoPoint(
                        calculatedCentralPoint.latitude,
                        calculatedCentralPoint.longitude,
                    ),
                accuracy = averageAccuracy,
                altitude = null,
                time = null,
                updates = ++updates,
            )
    }

    private var isStable: Boolean = false

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun validateLocation(location: Location): Boolean {
        // check if lastLocations buffer is not full
        if (lastLocations.size < MAX_LOCATION_HISTORY) return true
        // calculate the distance of the location to the central point.
        val locCalc = location.distanceTo(calculatedCentralPoint)
        // if the distance is bigger or the equal to MAX_DRIFTED_METERS, evaluate the user activity
        if (locCalc >= MAX_DRIFTED_METERS) {
            return when (userActivity) {
                // if the user is on the movement check if the discardedLocations make sense
                "IN_VEHICLE", "ON_BICYCLE", "ON_FOOT", "WALKING", "RUNNING", "UNKNOWN" -> true
                // if the user is still or tilting, check if the discard locations make sense
                // check if are all in a radius
                "STILL", "TILTING" -> {
                    // if the discardedLocations are empty, not enough data to evaluate
                    if (discardedLocations.size < MAX_LOCATION_HISTORY_DISCARDED) {
                        // increase the update interval to increase the accuracy
                        stopLocationUpdates()
                        startLocationUpdates(
                            createLocationRequest(100.milliseconds.inWholeMilliseconds, Priority.PRIORITY_HIGH_ACCURACY),
                        )
                        return false
                    }
                    // check for the precision of the discarded locations and if the location
                    // is in the cluster TODO
                    if (discardedLocations.isClusteredWithin(MAX_DRIFTED_METERS) &&
                        location.isInCluster()
                    ) {
                        lastLocations.clear()
                        lastLocations.addAll(discardedLocations)
                        discardedLocations.clear()
                        isStable = true
                        possibleArea =
                            Area(
                                center = discardedLocationsCentralPoint,
                                radius = MAX_DRIFTED_METERS,
                            )
                        // decrease the update interval
                        stopLocationUpdates()
                        startLocationUpdates(
                            createLocationRequest(5000.milliseconds.inWholeMilliseconds, Priority.PRIORITY_BALANCED_POWER_ACCURACY),
                        )
                        Log.d("LocationManagerMine", "Locations stabilized")
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        } else {
            // if the location is within the area of the lastLocations
            // and the accuracy is lower than the average accuracy, save it
            return location.isInArea() && location.accuracy < averageAccuracy
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var locationJob: Job? = null
    private var active = false

    @RequiresPermission(
        allOf =
            [
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACTIVITY_RECOGNITION,
            ],
    )
    fun startUp() {
        if (!active) {
            active = true
            locationJob?.cancel()

            scope.launch {
                startListeningForActivityTransitions()
                startListeningForActivity()
            }
            locationJob =
                scope.launch {
                    val locationRequest =
                        createLocationRequest(
                            interval = 500.milliseconds.inWholeMilliseconds,
                            priority = Priority.PRIORITY_HIGH_ACCURACY,
                        )
                    startLocationUpdates(locationRequest)
                    listenForLocationUpdates()
                }
        }
    }

    fun stop() {
        if (active) {
            active = false
            locationJob?.cancel()
            stopLocationUpdates()
            lastLocations.clear()
            discardedLocations.clear()
            possibleArea = null
            userActivity = null
            isStable = false
        }
    }

    @RequiresPermission(
        allOf =
            [
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ],
    )
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

    @RequiresPermission(
        allOf =
            [
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ],
    )
    private suspend fun startListeningForActivity() {
        userInfo.lastActivity.collect { activity ->
            userActivity =
                UserActivityTransitionManager.Companion.getActivityType(activity)
            if (userActivity != "STILL" && userActivity != "TILTING") {
                isStable = false
                Log.d("LocationManagerMine", "Not stable")
                // increase updates interval
                stopLocationUpdates()
                startLocationUpdates(createLocationRequest(100.milliseconds.inWholeMilliseconds, Priority.PRIORITY_HIGH_ACCURACY))
            }
        }
    }

    private fun createLocationRequest(
        interval: Long,
        priority: Int,
    ): LocationRequest {
        return LocationRequest.Builder(priority, interval)
            .setIntervalMillis(interval)
            .setMinUpdateIntervalMillis(interval)
            .setWaitForAccurateLocation(true)
            .setPriority(priority)
            .setMaxUpdateDelayMillis(0)
            .build()
    }

    private fun stopLocationUpdates() {
        locationCallback.let {
            locationClient.removeLocationUpdates(it)
            Log.d("LocationManagerMine", "Stopped location updates")
        }
    }

    private fun List<Location>.calculatePrecision(): Float {
        return this.map { it.distanceTo(calculatedCentralPoint) }.average().toFloat()
    }

    private fun ArrayList<Location>.isClusteredWithin(radiusMeters: Float): Boolean {
        if (this.isEmpty()) return false
        val center = this.centralPoint()
        return this.all { it.distanceTo(center) <= radiusMeters }
    }

    private fun Location.isInArea(): Boolean = this.distanceTo(lastLocations.centralPoint()) < averageAccuracy

    private fun Location.isInCluster(): Boolean {
        if (discardedLocations.isEmpty()) return false
        val center = discardedLocations.centralPoint()
        val radius = discardedLocations.map { it.accuracy }.average().toFloat()
        return this.distanceTo(center) <= radius
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private suspend fun listenForLocationUpdates() {
        locationFlow.collect { location ->
            if (location != null) {
                if (validateLocation(location)) {
                    saveLocation(location)
                } else {
                    if (discardedLocations.size < MAX_LOCATION_HISTORY_DISCARDED) {
                        discardedLocations.add(location)
                    } else {
                        if (discardedLocations.size == MAX_LOCATION_HISTORY_DISCARDED) {
                            discardedLocations.removeAt(0)
                            discardedLocations.add(location)
                        }
                    }
                }
            }
        }
    }
}
