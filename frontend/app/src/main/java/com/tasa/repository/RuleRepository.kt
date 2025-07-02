package com.tasa.repository

import com.tasa.domain.ApiError
import com.tasa.domain.AuthenticationException
import com.tasa.domain.Event
import com.tasa.domain.Location
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocation
import com.tasa.domain.RuleLocationTimeless
import com.tasa.domain.UserInfoRepository
import com.tasa.repository.interfaces.RuleRepositoryInterface
import com.tasa.service.TasaService
import com.tasa.storage.TasaDB
import com.tasa.utils.Either
import com.tasa.utils.Failure
import com.tasa.utils.Success
import com.tasa.utils.failure
import com.tasa.utils.success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import kotlin.collections.filter
import kotlin.collections.map

class RuleRepository(
    private val local: TasaDB,
    private val remote: TasaService,
    private val userInfoRepository: UserInfoRepository,
) : RuleRepositoryInterface {
    private suspend fun getToken(): String {
        return userInfoRepository.getToken() ?: throw AuthenticationException(
            "User is not authenticated. Please log in again.",
            null,
        )
    }

    private suspend fun getFromApi() = remote.ruleService.fetchRules(getToken())

    private suspend fun hasRules(): Boolean {
        return local.ruleEventDao().hasRules() && local.ruleLocationDao().hasRules()
    }

    override suspend fun fetchAllRules(): Either<ApiError, Flow<List<Rule>>> {
        return if (hasRules()) {
            success(getFromLocal())
        } else {
            when (val result = getFromApi()) {
                is Success -> {
                    local.ruleEventDao().insertRuleEvents(
                        result.value.filterIsInstance<RuleEvent>().map { it.toRuleEventEntity() },
                    )
                    local.ruleLocationDao().insertRuleLocations(
                        result.value.filterIsInstance<RuleLocation>().map { it.toRuleLocationEntity() },
                    )
                    success(getFromLocal())
                }
                is Failure -> failure(result.value)
            }
        }
    }

    private fun getFromLocal(): Flow<List<Rule>> {
        val now = LocalDateTime.now()
        val ruleEventFlow =
            local.ruleEventDao().getAllRuleEvents()
                .map { list ->
                    list.map { it.toRuleEvent() }
                        .filter { it.endTime.isAfter(now) }.sortedBy { it.startTime }
                }

        val ruleLocationFlow =
            local.ruleLocationDao().getAllRuleLocations()
                .map { list ->
                    list.map { it.toRuleLocation() }
                        .filter { it.endTime.isAfter(now) }.sortedBy { it.startTime }
                }

        val timelessRuleLocationFlow =
            local.ruleLocationTimelessDao().getAllRuleLocations()
                .map { list ->
                    list.map { it.toRuleLocationTimeless() }
                }

        return combine(
            ruleEventFlow,
            ruleLocationFlow,
            timelessRuleLocationFlow,
        ) { events, locations, timeless ->
            (events + locations + timeless)
        }
    }

    override suspend fun fetchRuleEvents(): Either<ApiError, Flow<List<RuleEvent>>> {
        return if (local.ruleEventDao().hasRules()) {
            success(local.ruleEventDao().getAllRuleEvents().map { it.map { it.toRuleEvent() } })
        } else {
            when (val result = getFromApi()) {
                is Success -> {
                    local.ruleEventDao().insertRuleEvents(
                        result.value.filterIsInstance<RuleEvent>().map { it.toRuleEventEntity() },
                    )

                    success(local.ruleEventDao().getAllRuleEvents().map { it.map { it.toRuleEvent() } })
                }
                is Failure -> failure(result.value)
            }
        }
    }

    override suspend fun fetchRuleLocations(): Flow<List<RuleLocation>> {
        return local.ruleLocationDao().getAllRuleLocations().map { it.map { it.toRuleLocation() } }
    }

    override suspend fun fetchRuleLocationsByName(name: String): List<RuleLocation> {
        return local.ruleLocationDao().getRuleLocationsByLocationNameResult(name).map {
            it.toRuleLocation()
        }
    }

    override suspend fun fetchRuleByTime(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Rule? {
        val ruleEvent = local.ruleEventDao().getRuleEventByStartAndEndTime(startTime, endTime)
        val ruleLocation = local.ruleLocationDao().getRuleLocationByTime(startTime, endTime)
        if (ruleEvent != null) {
            return ruleEvent.toRuleEvent()
        }
        if (ruleLocation != null) {
            return ruleLocation.toRuleLocation()
        }
        return null
    }

    override suspend fun isCollision(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Boolean {
        val ruleEvent = local.ruleEventDao().getRuleEventByStartAndEndTime(startTime, endTime)
        val ruleLocation = local.ruleLocationDao().getRuleLocationByTime(startTime, endTime)
        return ruleEvent != null || ruleLocation != null
    }

    override suspend fun isCollisionWithAnother(
        rule: Rule,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
    ): Boolean {
        if (rule is RuleEvent) {
            val result =
                local.ruleEventDao().getRuleEventByStartAndEndTime(
                    newStartTime,
                    newEndTime,
                ) ?: return false
            return result != rule
        } else {
            val result =
                local.ruleLocationDao().getRuleLocationByTime(newStartTime, newEndTime)
                    ?: return false
            return result != rule
        }
    }

    override suspend fun insertRuleEvent(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        event: Event,
    ): RuleEvent {
        val ruleEvent =
            RuleEvent(
                startTime = startTime,
                endTime = endTime,
                event = event,
            )
        local.eventDao().insertEvents(event.toEventEntity())
        local.ruleEventDao().insertRuleEvent(ruleEvent.toRuleEventEntity())
        return ruleEvent
    }

    override suspend fun insertRuleLocation(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        location: Location,
    ): RuleLocation {
        val ruleLocation =
            RuleLocation(
                startTime = startTime,
                endTime = endTime,
                location = location,
            )
        local.ruleLocationDao().insertRuleLocation(ruleLocation.toRuleLocationEntity())
        return ruleLocation
    }

    override suspend fun deleteRuleEventById(id: Int) {
        local.ruleEventDao().deleteRuleEventById(id)
    }

    override suspend fun deleteRuleLocationById(id: Int) {
        local.ruleLocationDao().deleteRuleLocationById(id)
    }

    override suspend fun deleteRuleLocationByName(name: String) {
        local.ruleLocationDao().deleteRuleLocationByName(name)
    }

    override suspend fun deleteRuleEventByCalendarIdAndEventId(
        calendarId: Long,
        eventId: Long,
    ) {
        local.ruleEventDao().deleteRuleEventByEventIdAndCalendarId(eventId, calendarId)
    }

    override suspend fun cleanOldRules(now: LocalDateTime) {
        local.ruleEventDao().getAllRuleEvents().map { it.map { it.toRuleEvent() } }
            .collect { rules ->
                val rulesToDelete = rules.filter { it.endTime.isBefore(now) }
                val events = rulesToDelete.map { it.event }
                val eventsNotToDelete =
                    rules.filter { it !in rulesToDelete && it.event in events }.map { it.event }
                events.filter { it !in eventsNotToDelete }.forEach {
                    local.eventDao().deleteEvent(it.eventId, it.calendarId)
                }
                rulesToDelete.forEach {
                    local.ruleEventDao().deleteRuleEventByStartAndEndTime(it.startTime, it.endTime)
                }
            }
        local.ruleLocationDao().getAllRuleLocations().map { it.map { it.toRuleLocation() } }
            .collect { rules ->
                val rulesToDelete = rules.filter { it.endTime.isBefore(now) }
                rulesToDelete.forEach {
                    local.ruleLocationDao().deleteRuleLocationByName(it.location.name)
                }
            }
    }

    override suspend fun clean() {
        local.ruleEventDao().clear()
        local.ruleLocationDao().clear()
    }

    override suspend fun updateRuleEvent(
        id: Int?,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
        oldStartTime: LocalDateTime,
        oldEndTime: LocalDateTime,
    ) {
        if (id != null) {
            local.ruleEventDao().updateRuleEvent(id, newStartTime, newEndTime)
        } else {
            local.ruleEventDao().updateRuleEventByStartAndEndTime(
                newStartTime,
                newEndTime,
                oldStartTime,
                oldEndTime,
            )
        }
    }

    override suspend fun deleteRuleEventByEventIdAndCalendarIdAndStarTimeAndEndtime(
        eventId: Long,
        calendarId: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ) {
        local.ruleEventDao().deleteRuleEventByEventIdAndCalendarIdAndStarTimeAndEndTime(
            eventId,
            calendarId,
            startTime,
            endTime,
        )
    }

    override suspend fun getRulesForLocation(location: Location): List<RuleLocation> {
        val ruleLocations = local.ruleLocationDao().getRuleLocationsByLocationNameResult(location.name)
        return ruleLocations.map { it.toRuleLocation() }
    }

    override suspend fun getTimelessRulesForLocation(location: Location): List<RuleLocationTimeless> {
        return local.ruleLocationTimelessDao().getRuleLocationsByLocationNameResult(location.name)
            .map { it.toRuleLocationTimeless() }
    }

    override suspend fun insertRuleLocationTimeless(
        location: Location,
        geofenceId: Int,
    ): RuleLocationTimeless {
        val ruleLocationTimeless = RuleLocationTimeless(location = location)
        local.ruleLocationTimelessDao()
            .insertRuleLocationTimeless(ruleLocationTimeless.toEntity(geofenceId))
        return ruleLocationTimeless
    }

    override suspend fun getRuleLocationTimelessById(id: Int): RuleLocationTimeless? {
        TODO()
    }

    override suspend fun deleteRuleLocationTimelessByLocation(location: Location) {
        local.ruleLocationTimelessDao().deleteRuleLocationByName(location.name)
    }

    override suspend fun deleteRuleLocationTimelessById(id: Int) {
        local.ruleLocationTimelessDao().deleteRuleLocationById(id)
    }

    override suspend fun getAllRuleLocationTimeless(): Flow<List<RuleLocationTimeless>> {
        return local.ruleLocationTimelessDao().getAllRuleLocations()
            .map { it.map { it.toRuleLocationTimeless() } }
    }
}
