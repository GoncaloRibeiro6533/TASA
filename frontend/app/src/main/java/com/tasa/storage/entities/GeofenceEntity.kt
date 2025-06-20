package com.tasa.storage.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "geofence",
)
data class GeofenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
)
