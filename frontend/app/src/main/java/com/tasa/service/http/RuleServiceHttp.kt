package com.tasa.service.http

import com.tasa.domain.ApiError
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocation
import com.tasa.service.http.RuleServiceError.LocationIdNull
import com.tasa.service.http.models.rule.RuleEventOutput
import com.tasa.service.http.models.rule.RuleListOutput
import com.tasa.service.http.models.rule.RuleLocationOutput
import com.tasa.service.http.utils.delete
import com.tasa.service.http.utils.get
import com.tasa.service.http.utils.post
import com.tasa.service.http.utils.put
import com.tasa.service.interfaces.RuleService
import com.tasa.utils.Either
import com.tasa.utils.Failure
import com.tasa.utils.Success
import com.tasa.utils.failure
import com.tasa.utils.success
import io.ktor.client.HttpClient

sealed class RuleServiceError(message: String = "") : ApiError(message) {
    data object LocationIdNull : RuleServiceError()

    data object RuleIdNull : RuleServiceError()
}

class RuleServiceHttp(private val client: HttpClient) : RuleService {
    override suspend fun fetchRules(): Either<ApiError, List<Rule>> {
        return when (val response = client.get<RuleListOutput>("/rule/all")) {
            is Success -> success(response.value.toRules())
            is Failure -> failure(response.value)
        }
    }

    override suspend fun fetchRuleEventById(id: Int): Either<ApiError, RuleEvent> {
        return when (val response = client.get<RuleEventOutput>("/rule/event/$id")) {
            is Success -> success(response.value.toRuleEvent())
            is Failure -> failure(response.value)
        }
    }

    override suspend fun fetchRulesEventByCalendarIdAndEventId(
        calendarId: Long,
        eventId: Long,
    ): Either<ApiError, List<RuleEvent>> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchRuleLocationById(id: Int): Either<ApiError, RuleLocation> {
        return when (val response = client.get<RuleLocation>("/rule/location/$id")) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun fetchRulesLocationByName(name: String): Either<ApiError, List<RuleLocation>> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchRulesByTime(
        startTime: Long,
        endTime: Long,
    ): Either<ApiError, List<Rule>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertRuleEvent(ruleEvent: RuleEvent): Either<ApiError, RuleEvent> {
        return when (val response = client.post<RuleEventOutput>("/rule/event", body = ruleEvent.toRuleEventInput())) {
            is Success -> success(response.value.toRuleEvent())
            is Failure -> failure(response.value)
        }
    }

    override suspend fun insertRuleLocation(ruleLocation: RuleLocation): Either<ApiError, RuleLocation> {
        if (ruleLocation.location.id == null) return failure(LocationIdNull)
        return when (
            val response =
                client.post<RuleLocationOutput>(
                    "/rule/location",
                    body = ruleLocation.toRuleLocationInput(ruleLocation.location.id),
                )
        ) {
            is Success -> success(response.value.toRuleLocation())
            is Failure -> failure(response.value)
        }
    }

    override suspend fun insertRuleEvents(ruleEvents: List<RuleEvent>): Either<ApiError, List<RuleEvent>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertRuleLocations(ruleLocations: List<RuleLocation>): Either<ApiError, List<RuleLocation>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteRuleEventById(id: Int): Either<ApiError, Unit> {
        return when (val response = client.delete<Unit>("/rule/event/$id")) {
            is Success -> success(Unit)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun deleteRuleLocationById(id: Int): Either<ApiError, Unit> {
        return when (val response = client.delete<Unit>("/rule/location/$id")) {
            is Success -> success(Unit)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun updateRuleEvent(ruleEvent: RuleEvent): Either<ApiError, RuleEvent> {
        if (ruleEvent.id == null) return failure(RuleServiceError.RuleIdNull)
        return when (
            val response =
                client.put<RuleEventOutput>(
                    "/rule/event/${ruleEvent.id}/update/time",
                    body = ruleEvent.toRuleEventUpdateInput(),
                )
        ) {
            is Success -> success(response.value.toRuleEvent())
            is Failure -> failure(response.value)
        }
    }

    override suspend fun updateRuleLocation(ruleLocation: RuleLocation): Either<ApiError, RuleLocation> {
        if (ruleLocation.id == null) return failure(RuleServiceError.RuleIdNull)
        if (ruleLocation.location.id == null) return failure(LocationIdNull)
        return when (
            val response =
                client.put<RuleLocationOutput>(
                    "/rule/location/${ruleLocation.id}/update",
                    body = ruleLocation.toRuleLocationUpdateInput(),
                )
        ) {
            is Success -> success(response.value.toRuleLocation())
            is Failure -> failure(response.value)
        }
    }
}
