package com.tasa.service.http

import com.tasa.service.TasaService
import com.tasa.service.interfaces.EventService
import com.tasa.service.interfaces.LocationService
import com.tasa.service.interfaces.RuleService
import com.tasa.service.interfaces.UserService
import io.ktor.client.HttpClient

class TasaServiceHttp(private val client: HttpClient) : TasaService {
    override val userService: UserService by lazy {
        UserServiceHttp(client)
    }
    override val locationService: LocationService by lazy {
        LocationServiceHttp(client)
    }
    override val eventService: EventService by lazy {
        EventServiceHttp(client)
    }
    override val ruleService: RuleService by lazy {
        RuleServiceHttp(client)
    }
}
