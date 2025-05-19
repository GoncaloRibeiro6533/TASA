package com.tasa.storage.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Relation
import com.tasa.domain.RuleLocation
import java.time.LocalDateTime

@Entity(
    tableName = "rule_location",
    primaryKeys = ["startTime", "endTime"],
    foreignKeys = [
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["name"],
            childColumns = ["locationName"],
            // onDelete = ForeignKey.CASCADE
        ),
    ],
)
data class RuleLocationEntity(
    val id: Int? = null,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val locationName: String,
)

data class RuleLocationWithLocation(
    @Embedded val ruleLocation: RuleLocationEntity,
    @Relation(
        parentColumn = "locationName",
        entityColumn = "name",
    )
    val location: LocationEntity,
) {
    fun toRuleLocation(): RuleLocation {
        return RuleLocation(
            id = ruleLocation.id,
            startTime = ruleLocation.startTime,
            endTime = ruleLocation.endTime,
            location = location.toLocation(),
        )
    }
}
