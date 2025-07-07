package com.tasa.storage.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tasa.storage.entities.localMode.AlarmLocal
import com.tasa.storage.entities.localMode.EventLocal
import com.tasa.storage.entities.localMode.GeofenceLocal
import com.tasa.storage.entities.localMode.LocationLocal
import com.tasa.storage.entities.localMode.RuleEventLocal
import com.tasa.storage.entities.localMode.RuleEventWithEventLocal
import com.tasa.storage.entities.localMode.RuleLocationLocal
import com.tasa.storage.entities.localMode.RuleLocationLocalWithLocation
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface LocalDao {
    // CREATE

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocationLocal(location: LocationLocal): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEventLocal(event: EventLocal): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRuleLocationLocal(ruleLocation: RuleLocationLocal): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRuleEventLocal(ruleEvent: RuleEventLocal): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlarmLocal(alarm: AlarmLocal): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGeofenceLocal(geofence: GeofenceLocal): Long

    // READ

    @Query("SELECT * FROM location_local")
    suspend fun getAllLocations(): List<LocationLocal>

    @Query("SELECT * FROM location_local")
    fun getLocationsFlow(): Flow<List<LocationLocal>>

    @Query("SELECT * FROM location_local WHERE id = :id")
    suspend fun getLocationById(id: Long): LocationLocal?

    @Query("SELECT * FROM event_local")
    suspend fun getAllEvents(): List<EventLocal>

    @Query("SELECT * FROM event_local")
    fun getEventsFlow(): Flow<List<EventLocal>>

    @Query("SELECT * FROM event_local WHERE id = :id")
    suspend fun getEventById(id: Long): EventLocal?

    @Query("SELECT * FROM event_local WHERE calendarId = :calendarId AND eventId = :eventId")
    suspend fun getEventByCalendarIdAndEventId(
        calendarId: Long,
        eventId: Long,
    ): EventLocal?

    @Query(
        """
        SELECT rule_event_local.*, event_local.* FROM rule_event_local
        INNER JOIN event_local ON rule_event_local.externalId = event_local.id
    """,
    )
    suspend fun getAllRuleEventsWithEventLocal(): List<RuleEventWithEventLocal>

    @Query(
        """
        SELECT rule_event_local.*, event_local.* FROM rule_event_local
        INNER JOIN event_local ON rule_event_local.externalId = event_local.id
    """,
    )
    fun getAllRuleEventsWithEventLocalFlow(): Flow<List<RuleEventWithEventLocal>>

    @Query(
        """
        SELECT rule_event_local.*, event_local.* FROM rule_event_local
        INNER JOIN event_local ON rule_event_local.externalId = event_local.id 
        WHERE rule_event_local.id = :id
    """,
    )
    suspend fun getRuleEventWithEventLocalById(id: Int): RuleEventWithEventLocal?

    @Query(
        """
        SELECT rule_location_local.*, location_local.* FROM rule_location_local
        INNER JOIN location_local ON rule_location_local.locationId = location_local.id
    """,
    )
    suspend fun getAllRuleLocationsWithLocation(): List<RuleLocationLocalWithLocation>

    @Query(
        """
        SELECT rule_location_local.*, location_local.* FROM rule_location_local
        INNER JOIN location_local ON rule_location_local.locationId = location_local.id
    """,
    )
    fun getAllRuleLocationsWithLocationFlow(): Flow<List<RuleLocationLocalWithLocation>>

    @Query(
        """
        SELECT rule_location_local.*, location_local.* FROM rule_location_local
        INNER JOIN location_local ON rule_location_local.locationId = location_local.id 
        WHERE rule_location_local.id = :id
    """,
    )
    suspend fun getRuleLocationWithLocationById(id: Int): RuleLocationLocalWithLocation?

    @Query("SELECT * from geofence_local")
    suspend fun getAllGeofences(): List<GeofenceLocal>

    @Query("SELECT * from geofence_local")
    fun getGeofencesFlow(): Flow<List<GeofenceLocal>>

    @Query("SELECT * from geofence_local WHERE id = :id")
    suspend fun getGeofenceById(id: Int): GeofenceLocal?

    @Query("SELECT * from alarm_local")
    suspend fun getAllAlarms(): List<AlarmLocal>

    @Query("SELECT * from alarm_local")
    fun getAlarmsFlow(): Flow<List<AlarmLocal>>

    @Query("SELECT * from alarm_local WHERE id = :id")
    suspend fun getAlarmById(id: Int): AlarmLocal?

    @Query("SELECT * from alarm_local WHERE triggerTime = :currentTime")
    suspend fun getAlarmByTriggerTime(currentTime: Long): AlarmLocal?

    // UPDATE

    @Query("UPDATE location_local SET name = :name, latitude = :latitude, longitude = :longitude WHERE id = :id")
    suspend fun updateLocationLocal(
        id: Int,
        name: String,
        latitude: Double,
        longitude: Double,
    )

    @Query("UPDATE event_local SET eventId = :eventId, calendarId = :calendarId, title = :title WHERE id = :id")
    suspend fun updateEventLocal(
        id: Int,
        eventId: Long,
        calendarId: Long,
        title: String,
    )

    @Query("UPDATE rule_location_local SET locationId = :locationId WHERE id = :id")
    suspend fun updateRuleLocationLocal(
        id: Int,
        locationId: Long,
    )

    @Query("UPDATE rule_event_local SET startTime = :startTime, endTime = :endTime, externalId = :externalId WHERE id = :id")
    suspend fun updateRuleEventLocal(
        id: Int,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        externalId: Int,
    )

    @Query("UPDATE alarm_local SET triggerTime = :time WHERE id = :id")
    suspend fun updateAlarmLocal(
        id: Int,
        time: Long,
    )

    @Query("UPDATE geofence_local SET latitude = :latitude, longitude = :longitude, radius = :radius WHERE id = :id")
    suspend fun updateGeofenceLocal(
        id: Int,
        latitude: Double,
        longitude: Double,
        radius: Double,
    )

    // DELETE

    @Query("DELETE FROM location_local WHERE id = :id")
    suspend fun deleteLocationLocalById(id: Int)

    @Query("DELETE FROM event_local WHERE id = :id")
    suspend fun deleteEventLocalById(id: Int)

    @Query("DELETE FROM rule_location_local WHERE id = :id")
    suspend fun deleteRuleLocationLocalById(id: Int)

    @Query("DELETE FROM rule_event_local WHERE id = :id")
    suspend fun deleteRuleEventLocalById(id: Int)

    @Query("DELETE FROM geofence_local WHERE id = :id")
    suspend fun deleteGeofenceLocalById(id: Int)

    @Query("DELETE FROM alarm_local WHERE id = :id")
    suspend fun deleteAlarmLocalById(id: Int)

    @Query("DELETE FROM location_local")
    suspend fun clearLocations()

    @Query("DELETE FROM event_local")
    suspend fun clearEvents()

    @Query("DELETE FROM rule_location_local")
    suspend fun clearRuleLocations()

    @Query("DELETE FROM rule_event_local")
    suspend fun clearRuleEvents()

    @Query("DELETE FROM geofence_local")
    suspend fun clearGeofences()

    @Query("DELETE FROM alarm_local")
    suspend fun clearAlarms()

    @Query("DELETE FROM alarm_local WHERE triggerTime < :currentTime")
    suspend fun clearOldAlarms(currentTime: Long)

    // CONDITIONAL

    @Query("SELECT COUNT(*)>0 FROM event_local")
    suspend fun hasEvents(): Boolean

    @Query("SELECT COUNT(*)>0 FROM location_local")
    suspend fun hasLocations(): Boolean

    @Query("SELECT COUNT(*)>0 FROM rule_event_local")
    suspend fun hasRuleEvents(): Boolean

    @Query("SELECT COUNT(*)>0 FROM rule_location_local")
    suspend fun hasRuleLocations(): Boolean

    @Query("SELECT COUNT(*)>0 FROM geofence_local")
    suspend fun hasGeofences(): Boolean

    @Query("SELECT COUNT(*)>0 FROM alarm_local")
    suspend fun hasAlarms(): Boolean

    @Query("SELECT COUNT(*)>0 FROM location_local WHERE id = :id")
    suspend fun hasLocationById(id: Int): Boolean

    @Query("SELECT * FROM location_local WHERE name = :name")
    suspend fun getLocationByName(name: String): LocationLocal?
}
