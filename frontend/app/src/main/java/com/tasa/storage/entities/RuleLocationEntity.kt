package com.tasa.storage.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Relation
import com.tasa.domain.Location

@Entity(
    tableName = "rule_location",
    primaryKeys = ["startTime", "endTime"],
    foreignKeys = [
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            // onDelete = ForeignKey.CASCADE
        ),
    ],
)
data class RuleLocationEntity(
    val id: Int? = null,
    val startTime: Long,
    val endTime: Long,
    val locationId: Int,
)

data class RuleLocationWithLocation(
    @Embedded val ruleLocation: RuleLocationEntity,
    @Relation(
        parentColumn = "locationId",
        entityColumn = "id",
    )
    val location: LocationEntity,
)
