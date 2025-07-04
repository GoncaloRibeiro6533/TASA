package com.tasa.utils

import android.content.Context
import android.location.Geocoder
import org.osmdroid.util.GeoPoint
import java.util.Locale
import kotlin.collections.isNotEmpty

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
)

interface SearchPlaceService {
    suspend fun searchPlace(
        query: String,
        range: Int,
    ): Coordinates?
}

class SearchPlaceServiceImpl(
    private val context: Context,
) : SearchPlaceService {
    override suspend fun searchPlace(
        query: String,
        range: Int,
    ): Coordinates? {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocationName(query, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            val point = GeoPoint(address.latitude, address.longitude)
            return Coordinates(latitude = point.latitude, longitude = point.longitude)
        }
        return null
    }
}
