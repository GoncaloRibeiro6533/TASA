package com.tasa.storage.entities.localMode

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.tasa.repository.interfaces.Geofence

@Entity(
    tableName = "geofence_local",
    foreignKeys = [
        ForeignKey(
            entity = RuleLocationLocal::class,
            parentColumns = ["id"],
            childColumns = ["ruleId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
)
data class GeofenceLocal(
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
            name = name,
            ruleId = ruleId,
        )
    }
}
