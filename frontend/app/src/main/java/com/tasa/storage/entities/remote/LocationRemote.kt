package com.tasa.storage.entities.remote

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tasa.domain.Location

@Entity(
    tableName = "location_remote",
)
data class LocationRemote(
    @PrimaryKey()
    val id: Int,
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
