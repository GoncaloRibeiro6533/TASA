package com.tasa.service.fake

import com.tasa.domain.ApiError
import com.tasa.domain.Event
import com.tasa.domain.Location
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocation
import com.tasa.service.RuleService
import com.tasa.utils.Either
import com.tasa.utils.failure
import com.tasa.utils.success
import java.time.LocalDateTime

class RuleServiceFake : RuleService {
    companion object {
        private var ruleEventId = 1
        private val ruleEvents =
            mutableListOf<RuleEvent>(
                RuleEvent(
                    id = 1,
                    startTime = LocalDateTime.parse("2025-06-01T10:00:00"),
                    endTime = LocalDateTime.parse("2025-06-01T12:00:00"),
                    event =
                        Event(
                            id = 1,
                            calendarId = 1,
                            title = "Event 1",
                        ),
                ),
            )
        private var ruleLocationId = 1
        private val ruleLocations =
            mutableListOf<RuleLocation>(
                RuleLocation(
                    id = 1,
                    startTime = LocalDateTime.parse("2025-06-01T10:00:00"),
                    endTime = LocalDateTime.parse("2025-06-01T12:00:00"),
                    location =
                        Location(
                            id = 1,
                            name = "ISEL",
                            latitude = 38.736946,
                            longitude = -9.142685,
                            radius = 100.0,
                        ),
                ),
            )
    }

    override suspend fun fetchRules(): Either<ApiError, List<Rule>> {
        return success(ruleLocations + ruleEvents)
    }

    override suspend fun fetchRuleEventById(id: Int): Either<ApiError, RuleEvent> {
        if (ruleEvents.isEmpty()) return failure(ApiError("RuleEvent not found"))
        val ruleEvent = ruleEvents.find { it.id == id }
        return if (ruleEvent != null) {
            success(ruleEvent)
        } else {
            failure(ApiError("RuleEvent not found"))
        }
    }

    override suspend fun fetchRulesEventByCalendarIdAndEventId(
        calendarId: Long,
        eventId: Long,
    ): Either<ApiError, List<RuleEvent>> {
        if (ruleEvents.isEmpty()) return failure(ApiError("RuleEvent not found"))
        val ruleEvent = ruleEvents.filter { it.event.calendarId == calendarId && it.event.id == eventId }
        return if (ruleEvent.isNotEmpty()) {
            success(ruleEvent)
        } else {
            failure(ApiError("RuleEvent not found"))
        }
    }

    override suspend fun fetchRuleLocationById(id: Int): Either<ApiError, RuleLocation> {
        if (ruleLocations.isEmpty()) return failure(ApiError("RuleLocation not found"))
        val ruleLocation = ruleLocations.find { it.id == id }
        return if (ruleLocation != null) {
            success(ruleLocation)
        } else {
            failure(ApiError("RuleLocation not found"))
        }
    }

    override suspend fun fetchRulesLocationByName(name: String): Either<ApiError, List<RuleLocation>> {
        if (ruleLocations.isEmpty()) return failure(ApiError("RuleLocation not found"))
        val ruleLocation = ruleLocations.filter { it.location.name == name }
        return if (ruleLocation.isNotEmpty()) {
            success(ruleLocation)
        } else {
            failure(ApiError("RuleLocation not found"))
        }
    }

    override suspend fun fetchRulesByTime(
        startTime: Long,
        endTime: Long,
    ): Either<ApiError, List<Rule>> {
        val ruleEvents =
            ruleEvents.filter {
                it.startTime.toEpochSecond(null) >= startTime &&
                    it.endTime.toEpochSecond(null) <= endTime
            }
        val ruleLocations =
            ruleLocations.filter {
                it.startTime.toEpochSecond(null) >= startTime &&
                    it.endTime.toEpochSecond(null) <= endTime
            }
        return if (ruleEvents.isNotEmpty() || ruleLocations.isNotEmpty()) {
            success(ruleEvents + ruleLocations)
        } else {
            failure(ApiError("Rule not found"))
        }
    }

    override suspend fun insertRuleEvent(ruleEvent: RuleEvent): Either<ApiError, RuleEvent> {
        val newRuleEvent = ruleEvent.copy(id = ++ruleEventId) as RuleEvent
        ruleEvents.add(newRuleEvent)
        return success(newRuleEvent)
    }

    override suspend fun insertRuleLocation(ruleLocation: RuleLocation): Either<ApiError, RuleLocation> {
        val newRuleLocation = ruleLocation.copy(id = ++ruleLocationId) as RuleLocation
        ruleLocations.add(newRuleLocation)
        return success(newRuleLocation)
    }

    override suspend fun insertRuleEvents(ruleEvents: List<RuleEvent>): Either<ApiError, List<RuleEvent>> {
        val newRuleEvents = ruleEvents.map { it.copy(id = ++ruleEventId) } as List<RuleEvent>
        Companion.ruleEvents.addAll(newRuleEvents)
        return success(newRuleEvents)
    }

    override suspend fun insertRuleLocations(ruleLocations: List<RuleLocation>): Either<ApiError, List<RuleLocation>> {
        val newRuleLocations = ruleLocations.map { it.copy(id = ++ruleLocationId) } as List<RuleLocation>
        Companion.ruleLocations.addAll(newRuleLocations)
        return success(newRuleLocations)
    }

    override suspend fun deleteRuleEventById(id: Int): Either<ApiError, Unit> {
        val ruleEvent = ruleEvents.find { it.id == id }
        return if (ruleEvent != null) {
            ruleEvents.remove(ruleEvent)
            success(Unit)
        } else {
            failure(ApiError("RuleEvent not found"))
        }
    }

    override suspend fun deleteRuleLocationById(id: Int): Either<ApiError, Unit> {
        val ruleLocation = ruleLocations.find { it.id == id }
        return if (ruleLocation != null) {
            ruleLocations.remove(ruleLocation)
            success(Unit)
        } else {
            failure(ApiError("RuleLocation not found"))
        }
    }
}
