package com.tasa.storage.entities.remote

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.tasa.domain.RuleLocationTimeless

@Entity(
    tableName = "rule_location_remote",
    foreignKeys = [
        ForeignKey(
            entity = LocationRemote::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
)
data class RuleLocationRemote(
    @PrimaryKey
    val id: Int,
    val locationId: Int,
)

data class RuleLocationRemoteWithLocation(
    @Embedded val ruleLocation: RuleLocationRemote,
    @Relation(
        parentColumn = "locationId",
        entityColumn = "id",
    )
    val location: LocationRemote,
) {
    fun toRuleLocationTimeless(): RuleLocationTimeless {
        return RuleLocationTimeless(
            id = ruleLocation.id,
            location = location.toLocation(),
        )
    }
}
