package com.tasa.storage.entities.remote

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.tasa.repository.interfaces.Geofence

@Entity(
    tableName = "geofence_remote",
    foreignKeys = [
        ForeignKey(
            entity = RuleLocationRemote::class,
            parentColumns = ["id"],
            childColumns = ["ruleId"],
        ),
    ],
)
data class GeofenceRemote(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
    val ruleId: Int,
    val name: String,
) {
    fun toGeofence(): Geofence {
        return Geofence(
            id = id,
            latitude = latitude,
            longitude = longitude,
            radius = radius,
            ruleId = ruleId,
            name = name,
        )
    }
}
