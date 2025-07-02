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
import java.time.LocalDateTime

sealed class RuleServiceError(message: String = "") : ApiError(message) {
    data object LocationIdNull : RuleServiceError()

    data object RuleIdNull : RuleServiceError()

    data object EventIdNull : RuleServiceError()
}

class RuleServiceHttp(private val client: HttpClient) : RuleService {
    override suspend fun fetchRules(token: String): Either<ApiError, List<Rule>> {
        return when (val response = client.get<RuleListOutput>("/rule/all", token = token)) {
            is Success -> success(response.value.toRules())
            is Failure -> failure(response.value)
        }
    }

    override suspend fun fetchRuleEventById(
        id: Int,
        token: String,
    ): Either<ApiError, RuleEvent> {
        return when (val response = client.get<RuleEventOutput>("/rule/event/$id", token = token)) {
            is Success -> success(response.value.toRuleEvent())
            is Failure -> failure(response.value)
        }
    }

    override suspend fun fetchRuleLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, RuleLocation> {
        return when (val response = client.get<RuleLocation>("/rule/location/$id", token = token)) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun insertRuleEvent(
        ruleEvent: RuleEvent,
        token: String,
    ): Either<ApiError, RuleEvent> {
        if (ruleEvent.event.id == null) return failure(RuleServiceError.EventIdNull)
        return when (
            val response =
                client.post<RuleEventOutput>(
                    "/rule/event",
                    body = ruleEvent.toRuleEventInput(ruleEvent.event.id),
                    token = token,
                )
        ) {
            is Success -> success(response.value.toRuleEvent())
            is Failure -> failure(response.value)
        }
    }

    override suspend fun insertRuleLocation(
        ruleLocation: RuleLocation,
        token: String,
    ): Either<ApiError, RuleLocation> {
        if (ruleLocation.location.id == null) return failure(LocationIdNull)
        return when (
            val response =
                client.post<RuleLocationOutput>(
                    "/rule/location",
                    body = ruleLocation.toRuleLocationInput(ruleLocation.location.id),
                    token = token,
                )
        ) {
            is Success -> success(response.value.toRuleLocation())
            is Failure -> failure(response.value)
        }
    }

    override suspend fun deleteRuleEventById(
        id: Int,
        token: String,
    ): Either<ApiError, Unit> {
        return when (val response = client.delete<Unit>("/rule/event/$id", token = token)) {
            is Success -> success(Unit)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun deleteRuleLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, Unit> {
        return when (val response = client.delete<Unit>("/rule/location/$id", token = token)) {
            is Success -> success(Unit)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun updateRuleEvent(
        ruleEvent: RuleEvent,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
        token: String,
    ): Either<ApiError, RuleEvent> {
        if (ruleEvent.id == null) return failure(RuleServiceError.RuleIdNull)
        return when (
            val response =
                client.put<RuleEventOutput>(
                    "/rule/event/${ruleEvent.id}/update/time",
                    body = ruleEvent.toRuleEventUpdateInput(),
                    token = token,
                )
        ) {
            is Success -> success(response.value.toRuleEvent())
            is Failure -> failure(response.value)
        }
    }

    override suspend fun updateRuleLocation(
        ruleLocation: RuleLocation,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
        token: String,
    ): Either<ApiError, RuleLocation> {
        if (ruleLocation.id == null) return failure(RuleServiceError.RuleIdNull)
        if (ruleLocation.location.id == null) return failure(LocationIdNull)
        return when (
            val response =
                client.put<RuleLocationOutput>(
                    "/rule/location/${ruleLocation.id}/update",
                    body = ruleLocation.toRuleLocationUpdateInput(),
                    token = token,
                )
        ) {
            is Success -> success(response.value.toRuleLocation())
            is Failure -> failure(response.value)
        }
    }
}
