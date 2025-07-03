package com.tasa.storage.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "geofence",
    foreignKeys = [
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["name"],
            childColumns = ["name"],
        ),
    ],
    indices = [Index(value = ["name"])],
)
data class GeofenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
)
