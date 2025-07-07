package com.tasa.storage.entities.localMode

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tasa.domain.Location

@Entity(
    tableName = "location_local",
)
data class LocationLocal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
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
