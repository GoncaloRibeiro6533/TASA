package com.tasa.domain.location

import org.osmdroid.util.GeoPoint

class Location(
    val id: Int,
    val name: String,
    val center: GeoPoint,
    val radius: Int,
    val adress: String,
)
