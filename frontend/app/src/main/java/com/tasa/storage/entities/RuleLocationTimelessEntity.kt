package com.tasa.storage.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Relation
import com.tasa.domain.RuleLocationTimeless

@Entity(
    tableName = "rule_location_timeless",
    primaryKeys = ["locationName"],
    foreignKeys = [
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["name"],
            childColumns = ["locationName"],
            // onDelete = ForeignKey.CASCADE
        ),
    ],
)
data class RuleLocationTimelessEntity(
    val id: Int? = null,
    val locationName: String,
)

data class RuleLocationTimelessWithLocation(
    @Embedded val ruleLocation: RuleLocationTimelessEntity,
    @Relation(
        parentColumn = "locationName",
        entityColumn = "name",
    )
    val location: LocationEntity,
) {
    fun toRuleLocationTimeless(): RuleLocationTimeless {
        return RuleLocationTimeless(
            id = ruleLocation.id,
            location = location.toLocation(),
        )
    }
}
