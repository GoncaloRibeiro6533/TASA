package com.tasa.domain.location

import org.osmdroid.util.GeoPoint

class Location(
    val id: Int,
    val name: String,
    val center: GeoPoint,
    val radius: Int,
    val adress: String,
)

data class LocationTasa(
    val id: Int? = null,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val center: GeoPoint,
    val radius: Float,
)
