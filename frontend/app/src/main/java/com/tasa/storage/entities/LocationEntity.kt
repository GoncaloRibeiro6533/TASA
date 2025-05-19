package com.tasa.storage.entities

import androidx.room.Entity
import com.tasa.domain.Location

@Entity(
    tableName = "location",
    primaryKeys = ["name"],
)
data class LocationEntity(
    val id: Int? = null,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
) {
    fun toLocation(): Location {
        return Location(
            id = id,
            name = name,
            latitude = latitude,
            longitude = longitude,
            radius = radius,
        )
    }
}
