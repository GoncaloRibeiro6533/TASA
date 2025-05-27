package com.tasa.service.http

import com.tasa.service.EventService
import com.tasa.service.LocationService
import com.tasa.service.RuleService
import com.tasa.service.TasaService
import com.tasa.service.UserService
import io.ktor.client.HttpClient

class TasaServiceHttp(private val client: HttpClient) : TasaService {
    override val userService: UserService by lazy {
        UserServiceHttp(client)
    }
    override val locationService: LocationService by lazy {
        TODO()
    }
    override val eventService: EventService by lazy {
        TODO()
    }
    override val ruleService: RuleService by lazy {
        TODO()
    }

  /*  val locationService = LocationServiceHttp(client)
    val eventService = EventServiceHttp(client)
    val alarmService = AlarmServiceHttp(client)
    val ruleService = RuleServiceHttp(client)*/
}
