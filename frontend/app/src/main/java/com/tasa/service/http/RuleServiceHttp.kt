package com.tasa.service.http

import com.tasa.domain.ApiError
import com.tasa.domain.Event
import com.tasa.domain.Location
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocationTimeless
import com.tasa.service.http.models.rule.RuleEventInput
import com.tasa.service.http.models.rule.RuleEventOutput
import com.tasa.service.http.models.rule.RuleListOutput
import com.tasa.service.http.models.rule.RuleLocationInput
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

class RuleServiceHttp(private val client: HttpClient) : RuleService {
    override suspend fun fetchRules(token: String): Either<ApiError, RuleListOutput> {
        return when (val response = client.get<RuleListOutput>("/rule/all", token = token)) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun fetchRuleEventById(
        id: Int,
        token: String,
    ): Either<ApiError, RuleEventOutput> {
        return when (val response = client.get<RuleEventOutput>("/rule/event/$id", token = token)) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun fetchRuleLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, RuleLocationTimeless> {
        return when (val response = client.get<RuleLocationTimeless>("/rule/location/$id", token = token)) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun insertRuleEvent(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        event: Event,
        token: String,
    ): Either<ApiError, RuleEvent> {
        return when (
            val response =
                client.post<RuleEventOutput>(
                    "/rule/event",
                    body =
                        RuleEventInput(
                            startTime = startTime,
                            endTime = endTime,
                            eventId = event.id,
                        ),
                    token = token,
                )
        ) {
            is Success -> success(response.value.toRuleEvent(event))
            is Failure -> failure(response.value)
        }
    }

    override suspend fun insertRuleLocation(
        location: Location,
        token: String,
    ): Either<ApiError, RuleLocationTimeless> {
        return when (
            val response =
                client.post<RuleLocationOutput>(
                    "/rule/location",
                    body =
                        RuleLocationInput(
                            locationId = location.id,
                        ),
                    token = token,
                )
        ) {
            is Success -> success(response.value.toRuleLocationTimeless())
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
        return when (
            val response =
                client.put<RuleEventOutput>(
                    "/rule/event/${ruleEvent.id}/update/time",
                    body = ruleEvent.toRuleEventUpdateInput(),
                    token = token,
                )
        ) {
            is Success -> success(response.value.toRuleEvent(ruleEvent.event))
            is Failure -> failure(response.value)
        }
    }
}
