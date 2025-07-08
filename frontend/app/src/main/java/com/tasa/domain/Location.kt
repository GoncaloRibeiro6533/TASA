package com.tasa.domain

import android.location.Location
import android.os.Parcelable
import com.tasa.service.http.models.location.LocationInput
import com.tasa.storage.entities.localMode.LocationLocal
import com.tasa.storage.entities.remote.LocationRemote
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Represents a location defined by its name, latitude and longitude.
 * @property id the location's id
 * @property name the location's name
 * @property latitude the location's latitude
 * @property longitude the location's longitude
 * @property radius the radius of the location
 * @throws IllegalArgumentException if any of the parameters is invalid
 */

@Serializable
@Parcelize
data class Location(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
): Parcelable {
    init {

        require(name.isNotBlank()) { "name must not be blank" }
        require(name.length <= MAX_NAME_LENGTH) {
            "name must not be longer than $MAX_NAME_LENGTH"
        }
        require(latitude in MIN_LATITUDE..MAX_LATITUDE) {
            "latitude must be between $MIN_LATITUDE and $MAX_LATITUDE"
        }
        require(longitude in MIN_LONGITUDE..MAX_LONGITUDE) {
            "longitude must be between $MIN_LONGITUDE and $MAX_LONGITUDE"
        }
        require(radius > 0) { "radius must be positive" }
    }

    companion object {
        const val MAX_NAME_LENGTH = 50
        const val MAX_LATITUDE = 90.0
        const val MIN_LATITUDE = -90.0
        const val MAX_LONGITUDE = 180.0
        const val MIN_LONGITUDE = -180.0
    }

    fun toLocationLocal(): LocationLocal {
        return LocationLocal(
            id = id,
            name = name,
            latitude = latitude,
            longitude = longitude,
            radius = radius,
        )
    }

    fun toLocationRemote(): LocationRemote {
        return LocationRemote(
            id = id,
            name = name,
            latitude = latitude,
            longitude = longitude,
            radius = radius,
        )
    }

    fun toLocation(): Location {
        return Location("").apply {
            latitude = this@Location.latitude
            longitude = this@Location.longitude
            accuracy = this@Location.radius.toFloat()
        }
    }

    fun toLocationInput(): LocationInput {
        return LocationInput(
            name = name,
            latitude = latitude,
            longitude = longitude,
            radius = radius,
        )
    }
}
