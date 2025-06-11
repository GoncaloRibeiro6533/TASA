package com.tasa.storage.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "geofence",
    indices = [Index(value = ["name"], unique = true)],
)
data class GeofenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
)
