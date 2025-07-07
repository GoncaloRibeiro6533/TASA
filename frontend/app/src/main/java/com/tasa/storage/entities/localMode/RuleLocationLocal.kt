package com.tasa.storage.entities.localMode

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.tasa.domain.RuleLocationTimeless

@Entity(
    tableName = "rule_location_local",
    foreignKeys = [
        ForeignKey(
            entity = LocationLocal::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class RuleLocationLocal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val locationId: Int,
)

data class RuleLocationLocalWithLocation(
    @Embedded val ruleLocation: RuleLocationLocal,
    @Relation(
        parentColumn = "locationId",
        entityColumn = "id",
    )
    val location: LocationLocal,
) {
    fun toRuleLocationTimeless(): RuleLocationTimeless {
        return RuleLocationTimeless(
            id = ruleLocation.id,
            location = location.toLocation(),
        )
    }
}
