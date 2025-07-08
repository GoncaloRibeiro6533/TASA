package com.tasa.storage.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tasa.storage.entities.remote.AlarmRemote
import com.tasa.storage.entities.remote.EventRemote
import com.tasa.storage.entities.remote.GeofenceRemote
import com.tasa.storage.entities.remote.LocationRemote
import com.tasa.storage.entities.remote.RuleEventRemote
import com.tasa.storage.entities.remote.RuleEventWithEventRemote
import com.tasa.storage.entities.remote.RuleLocationRemote
import com.tasa.storage.entities.remote.RuleLocationRemoteWithLocation
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface RemoteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocationRemote(vararg location: LocationRemote)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEventRemote(vararg event: EventRemote)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRuleLocationRemote(vararg ruleLocation: RuleLocationRemote)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRuleEventRemote(vararg ruleEvent: RuleEventRemote)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlarmRemote(alarm: AlarmRemote): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGeofenceRemote(geofence: GeofenceRemote): Long

    // READ

    @Query("SELECT * FROM location_remote")
    suspend fun getAllLocations(): List<LocationRemote>

    @Query("SELECT * FROM location_remote")
    fun getLocationsFlow(): Flow<List<LocationRemote>>

    @Query("SELECT * FROM location_remote WHERE id = :id")
    suspend fun getLocationById(id: Int): LocationRemote?

    @Query("SELECT * FROM event_remote")
    suspend fun getAllEvents(): List<EventRemote>

    @Query("SELECT * FROM event_remote")
    fun getEventsFlow(): Flow<List<EventRemote>>

    @Query("SELECT * FROM event_remote WHERE id = :id")
    suspend fun getEventById(id: Int): EventRemote?

    @Query("SELECT * FROM event_remote WHERE calendarId = :calendarId AND eventId = :eventId")
    suspend fun getEventByCalendarIdAndEventId(
        calendarId: Long,
        eventId: Long,
    ): EventRemote?

    @Query(
        """
        SELECT rule_event_remote.*, event_remote.* FROM rule_event_remote
        INNER JOIN event_remote ON rule_event_remote.eventId = event_remote.id
    """,
    )
    suspend fun getAllRuleEventsWithEventRemote(): List<RuleEventWithEventRemote>

    @Query(
        """
        SELECT rule_event_remote.*, event_remote.* FROM rule_event_remote
        INNER JOIN event_remote ON rule_event_remote.eventId = event_remote.id
    """,
    )
    fun getAllRuleEventsWithEventRemoteFlow(): Flow<List<RuleEventWithEventRemote>>

    @Query(
        """
        SELECT rule_event_remote.*, event_remote.* FROM rule_event_remote
        INNER JOIN event_remote ON rule_event_remote.eventId = event_remote.id 
        WHERE rule_event_remote.id = :id
    """,
    )
    suspend fun getRuleEventWithEventRemoteById(id: Int): RuleEventWithEventRemote?

    @Query(
        """
        SELECT rule_location_remote.*, location_remote.* FROM rule_location_remote
        INNER JOIN location_remote ON rule_location_remote.locationId = location_remote.id
    """,
    )
    suspend fun getAllRuleLocationsWithLocation(): List<RuleLocationRemoteWithLocation>

    @Query(
        """
        SELECT rule_location_remote.*, location_remote.* FROM rule_location_remote
        INNER JOIN location_remote ON rule_location_remote.locationId = location_remote.id
    """,
    )
    fun getAllRuleLocationsWithLocationFlow(): Flow<List<RuleLocationRemoteWithLocation>>

    @Query(
        """
        SELECT rule_location_remote.*, location_remote.* FROM rule_location_remote
        INNER JOIN location_remote ON rule_location_remote.locationId = location_remote.id 
        WHERE rule_location_remote.id = :id
    """,
    )
    suspend fun getRuleLocationWithLocationById(id: Int): RuleLocationRemoteWithLocation?

    @Query("SELECT * from geofence_remote")
    suspend fun getAllGeofences(): List<GeofenceRemote>

    @Query("SELECT * from geofence_remote")
    fun getGeofencesFlow(): Flow<List<GeofenceRemote>>

    @Query("SELECT * from geofence_remote WHERE id = :id")
    suspend fun getGeofenceById(id: Int): GeofenceRemote?

    @Query("SELECT * from alarm_remote")
    suspend fun getAllAlarms(): List<AlarmRemote>

    @Query("SELECT * from alarm_remote")
    fun getAlarmsFlow(): Flow<List<AlarmRemote>>

    @Query("SELECT * from alarm_remote WHERE id = :id")
    suspend fun getAlarmById(id: Int): AlarmRemote?

    @Query("SELECT * from alarm_remote WHERE triggerTime = :currentTime")
    suspend fun getAlarmByTriggerTime(currentTime: Long): AlarmRemote?

    @Query("SELECT * from alarm_remote WHERE ruleId = :ruleId")
    suspend fun getAlarmsByRuleId(ruleId: Int): List<AlarmRemote>

    // UPDATE

    @Query("UPDATE location_remote SET name = :name, latitude = :latitude, longitude = :longitude, radius= :radius  WHERE id = :id")
    suspend fun updateLocationRemote(
        id: Int,
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Double,
    )

    @Query("UPDATE event_remote SET eventId = :eventId, calendarId = :calendarId, title = :title WHERE id = :id")
    suspend fun updateEventRemote(
        id: Int,
        eventId: Long,
        calendarId: Long,
        title: String,
    )

    @Query("UPDATE rule_location_remote SET locationId = :locationId WHERE id = :id")
    suspend fun updateRuleLocationRemote(
        id: Int,
        locationId: Int,
    )

    @Query("UPDATE rule_event_remote SET startTime = :startTime, endTime = :endTime WHERE id = :id")
    suspend fun updateRuleEventRemote(
        id: Int,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    )

    @Query("UPDATE alarm_remote SET triggerTime = :time WHERE id = :id")
    suspend fun updateAlarmRemote(
        id: Int,
        time: Long,
    )

    @Query("UPDATE geofence_remote SET latitude = :latitude, longitude = :longitude, radius = :radius, name = :name WHERE id = :id")
    suspend fun updateGeofenceRemote(
        id: Int,
        latitude: Double,
        longitude: Double,
        radius: Double,
        name: String,
    )

    // DELETE

    @Query("DELETE FROM location_remote WHERE id = :id")
    suspend fun deleteLocationRemoteById(id: Int)

    @Query("DELETE FROM event_remote WHERE id = :id")
    suspend fun deleteEventRemoteById(id: Int)

    @Query("DELETE FROM rule_location_remote WHERE id = :id")
    suspend fun deleteRuleLocationRemoteById(id: Int)

    @Query("DELETE FROM rule_event_remote WHERE id = :id")
    suspend fun deleteRuleEventRemoteById(id: Int)

    @Query("DELETE FROM geofence_remote WHERE id = :id")
    suspend fun deleteGeofenceRemoteById(id: Int)

    @Query("DELETE FROM alarm_remote WHERE id = :id")
    suspend fun deleteAlarmRemoteById(id: Int)

    @Query("DELETE FROM location_remote")
    suspend fun clearLocations()

    @Query("DELETE FROM event_remote")
    suspend fun clearEvents()

    @Query("DELETE FROM rule_location_remote")
    suspend fun clearRuleLocations()

    @Query("DELETE FROM rule_event_remote")
    suspend fun clearRuleEvents()

    @Query("DELETE FROM geofence_remote")
    suspend fun clearGeofences()

    @Query("DELETE FROM alarm_remote")
    suspend fun clearAlarms()

    @Query("DELETE FROM alarm_remote WHERE triggerTime < :currentTime")
    suspend fun clearOldAlarms(currentTime: Long)

    // CONDITIONAL

    @Query("SELECT COUNT(*)>0 FROM event_remote")
    suspend fun hasEvents(): Boolean

    @Query("SELECT COUNT(*)>0 FROM location_remote")
    suspend fun hasLocations(): Boolean

    @Query("SELECT COUNT(*)>0 FROM rule_location_remote")
    suspend fun hasRuleLocations(): Boolean

    @Query("SELECT COUNT(*)>0 FROM rule_event_remote")
    suspend fun hasRuleEvents(): Boolean

    @Query("SELECT COUNT(*)>0 FROM geofence_remote")
    suspend fun hasGeofences(): Boolean

    @Query("SELECT COUNT(*)>0 FROM alarm_remote")
    suspend fun hasAlarms(): Boolean

    @Query("SELECT COUNT(*)>0 FROM location_remote WHERE id = :id")
    suspend fun hasLocationById(id: Int): Boolean

    @Query("SELECT * FROM location_remote WHERE name = :name")
    suspend fun getLocationByName(name: String): LocationRemote?
}
