package com.tasa.storage.entities

import androidx.room.Entity

@Entity(
    tableName = "location",
    primaryKeys = ["name"],
)
data class LocationEntity(
    val id: Int? = null,
    val name: Int,
    val latitude: Double,
    val longitude: Double,
    val radius: Int,
)
