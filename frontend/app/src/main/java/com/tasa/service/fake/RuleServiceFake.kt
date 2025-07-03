package com.tasa.service.fake

import com.tasa.domain.ApiError
import com.tasa.domain.Event
import com.tasa.domain.Location
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocationTimeless
import com.tasa.service.http.models.rule.RuleEventOutput
import com.tasa.service.http.models.rule.RuleListOutput
import com.tasa.service.http.models.rule.RuleLocationOutput
import com.tasa.service.interfaces.RuleService
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
                            eventId = 1,
                            calendarId = 1,
                            title = "Event 1",
                        ),
                ),
            )
        private var ruleLocationId = 1
        private val ruleLocations =
            mutableListOf<RuleLocationTimeless>(
                RuleLocationTimeless(
                    id = 1,
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

    override suspend fun fetchRules(token: String): Either<ApiError, RuleListOutput> {
        return success(
            RuleListOutput(
                eventRulesN = 1,
                eventRules = emptyList<RuleEventOutput>(),
                locationRulesN = 0,
                locationRules = emptyList<RuleLocationOutput>(),
            ),
        )
    }

    override suspend fun fetchRuleEventById(
        id: Int,
        token: String,
    ): Either<ApiError, RuleEventOutput> {
        if (ruleEvents.isEmpty()) return failure(ApiError("RuleEvent not found"))
        val ruleEvent = ruleEvents.find { it.id == id }

        return failure(ApiError("RuleEvent not found"))
    }

    override suspend fun fetchRuleLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, RuleLocationTimeless> {
        if (ruleLocations.isEmpty()) return failure(ApiError("RuleLocation not found"))
        val ruleLocation = ruleLocations.find { it.id == id }
        return if (ruleLocation != null) {
            success(ruleLocation)
        } else {
            failure(ApiError("RuleLocation not found"))
        }
    }

    override suspend fun insertRuleEvent(
        ruleEvent: RuleEvent,
        token: String,
    ): Either<ApiError, RuleEvent> {
        val newRuleEvent = ruleEvent.copy(id = ++ruleEventId)
        ruleEvents.add(newRuleEvent)
        return success(newRuleEvent)
    }

    override suspend fun insertRuleLocation(
        ruleLocation: RuleLocationTimeless,
        token: String,
    ): Either<ApiError, RuleLocationTimeless> {
        val newRuleLocation = ruleLocation.copy(id = ++ruleLocationId)
        ruleLocations.add(newRuleLocation)
        return success(newRuleLocation)
    }

    override suspend fun deleteRuleEventById(
        id: Int,
        token: String,
    ): Either<ApiError, Unit> {
        val ruleEvent = ruleEvents.find { it.id == id }
        return if (ruleEvent != null) {
            ruleEvents.remove(ruleEvent)
            success(Unit)
        } else {
            failure(ApiError("RuleEvent not found"))
        }
    }

    override suspend fun deleteRuleLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, Unit> {
        val ruleLocation = ruleLocations.find { it.id == id }
        return if (ruleLocation != null) {
            ruleLocations.remove(ruleLocation)
            success(Unit)
        } else {
            failure(ApiError("RuleLocation not found"))
        }
    }

    override suspend fun updateRuleEvent(
        ruleEvent: RuleEvent,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
        token: String,
    ): Either<ApiError, RuleEvent> {
        val index = ruleEvents.indexOfFirst { it.id == ruleEvent.id }
        return if (index != -1) {
            ruleEvents[index] = ruleEvent
            success(ruleEvent)
        } else {
            failure(ApiError("RuleEvent not found"))
        }
    }
}
